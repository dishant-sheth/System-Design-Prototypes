package src;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import src.exceptions.UserAlreadyExistsException;
import src.interfaces.IUserManager;
import src.interfaces.IUserPortfolioManager;
import src.models.User;

public class UserRepository implements IUserManager {

    final Map<String, User> userMap;
    public UserRepository(){
        this.userMap = new ConcurrentHashMap<>();
    }

    @Override
    public User findUser(String userId) {
        return null;
    }

    @Override
    public void registerUser(String userId, String name, double initialBalance) {
        final User user = new User(userId, name);
        user.getPortfolio().updateBalance(initialBalance);
        
        if(userMap.putIfAbsent(userId, user) != null){
            throw new UserAlreadyExistsException(userId);
        }
    }

    @Override
    public boolean isValidUser(String userId){
        return userMap.containsKey(userId);
    }

    @Override
    public IUserPortfolioManager getUserPortfolioManager(String userId){
        return userMap.get(userId).getPortfolio();
    }
    
}