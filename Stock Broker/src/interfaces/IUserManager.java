package src.interfaces;

import src.models.User;

public interface IUserManager {
    public boolean isValidUser(String userId);
    public User findUser(String userId);
    public void registerUser(String userId, String name, double initialBalance);
    public IUserPortfolioManager getUserPortfolioManager(String userId);
}