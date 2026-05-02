## Payment Data Processing Engine

You are building a core component of a payment infrastructure platform. The system receives raw event logs from merchants, validates and parses them, detects anomalies, and produces audit-ready reports. It is designed to be extended over time — new event types, new fraud rules, new output formats.

### Part 1 — Event log parser & validator

Input format:
You receive a list of raw log lines. Each line is one of two event types:

> CHARGE   <txn_id> <merchant_id> <currency> <amount> <timestamp>
> DISPUTE  <txn_id> <merchant_id> <reason_code>       <timestamp>
>
> Examples:
> CHARGE   txn_A1B2C3D4 merch_stripe USD   49.99  2024-01-15T10:23:00
> DISPUTE  txn_A1B2C3D4 merch_stripe RC104        2024-01-15T11:00:00
> CHARGE   txn_ZZZZZZZZ merch_apple  GBP   0.00   2024-01-15T10:24:00
> CHARGE   txn_########  merch_bad   USD   -5.00  2024-01-15T10:25:00

### Part 2 — Sliding window fraud detector

Add a FraudDetector that operates on the stream of ChargeEvent objects (in timestamp order) and flags merchants as suspicious under the following rules:

Rule A — High velocity: A merchant has more than K charges within any rolling window of W seconds.
Rule B — High value burst: The total amount charged by a merchant in any rolling window of W seconds exceeds $LIMIT.

Both K, W, and LIMIT are configurable at construction time.

### Part 3 — Transaction ID codec

Stripe's internal systems use a compact encoding for transaction IDs to reduce storage. You need to implement a codec.

Encoding rules:
A transaction ID (stripped of the txn_ prefix) is a string of uppercase letters and digits, e.g. AAABBBCCCD. The encoder produces a run-length encoded string:

Consecutive identical characters are replaced with <count><char> if count > 1, or just <char> if count is 1.
AAABBBCCCD → 3A3B3CD
ABCD → ABCD (no compression, counts of 1 are omitted)
AABBAAB → 2A2B2AB

The decoder must reverse this exactly.

Additionally, Stripe wraps groups of encoded segments in brackets for versioning:

v1[3A3B]v2[2CD] means: decode 3A3B under version 1 rules, decode 2CD under version 2 rules, concatenate results.
For this problem, both versions use the same decoding rules — the brackets are purely structural and must be parsed correctly (they can nest).

### Part 4 — Merchant deduplication & audit report

Merchant IDs in the logs are self-reported and sometimes contain typos. Two merchant IDs are considered the same merchant if:

After stripping the merch_ prefix and normalising (lowercase, remove non-alphanumeric), they are identical, or
Their normalised names are within edit distance 1 of each other (one insert, delete, or replace).

Implement a MerchantDeduplicator that, given a list of merchant IDs, returns a Map<String, List<String>> grouping them — key is the canonical (first-seen) merchant ID, value is the list of all IDs in that group.
Then implement an AuditReporter that, given:

The full list of parsed events (from Part 1)
The deduplication map (from this part)
The set of suspicious merchants (from Part 2)

Produces a report per canonical merchant:
=== AUDIT REPORT ===
Merchant: merch_stripe (aliases: merch_stripe, merch_stripe)
  Status: SUSPICIOUS
  Total charges: 4  |  Total amount: USD 119.96
  Total disputes: 1  |  Dispute rate: 25.00%
  Flagged rules: HIGH_VELOCITY

Merchant: merch_apple
  Status: CLEAN
  Total charges: 2  |  Total amount: GBP 120.00
  Total disputes: 0  |  Dispute rate: 0.00%