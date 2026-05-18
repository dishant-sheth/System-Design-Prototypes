package src.models;

public record Trade(
    String buyerUserId,
    String sellerUserId,
    String buyerOrderId,
    String sellerOrderId,
    String stock,
    double price,
    double buyerOfferedPrice,
    int quantity,
    long tradeExecutedAt
){}