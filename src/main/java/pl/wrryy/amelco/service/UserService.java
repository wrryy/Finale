package pl.wrryy.amelco.service;

import lombok.AllArgsConstructor;
import org.assertj.core.api.BDDAssertions;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.wrryy.amelco.entity.*;
import pl.wrryy.amelco.repository.RoleRepository;
import pl.wrryy.amelco.repository.UserRepository;
import pl.wrryy.amelco.repository.WalletEventRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WalletEventService walletEventService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, WalletEventService walletEventService, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.walletEventService = walletEventService;
        this.passwordEncoder = passwordEncoder;
    }

    public User findOne(long id) {
        return userRepository.findOne(id);
    }

    public User findByUserName(String name) {
        return userRepository.findByUserName(name);
    }

    public User findByUserNameLike(String name) {
        return userRepository.findUserByUserNameEquals(name);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public void deleteUser(long id) {
        userRepository.delete(id);
    }

    public void registerUser(User user) {
        Role userRole = roleRepository.findByName("ROLE_USER");
        user.setRoles(new HashSet<>(Arrays.asList(userRole)));
        user.setActive(true);
        user.setWalletBalance(BigDecimal.valueOf(100));
        addUser(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void walletWithdraw(User user, BigDecimal value) {
        String event = "Withdrawal";
        WalletEvent walletEvent = walletEventService.createWalletEvent(event, user, value);
        walletEventService.saveWalletEvent(walletEvent);
        user.setWalletBalance(user.getWalletBalance().subtract(value));
        this.saveUser(user);
    }

    public void walletDeposit(User user, BigDecimal value) {
        String event = "Deposit";
        WalletEvent walletEvent = walletEventService.createWalletEvent(event, user, value);
        walletEventService.saveWalletEvent(walletEvent);
        user.setWalletBalance(user.getWalletBalance().add(value));
        this.saveUser(user);
    }

    /**
     * Increases user walletBalance in case of successful bet settlement.
     *
     * @param user
     * @param value
     */
    public void walletPay(User user, BigDecimal value) {
        String event = "Bet won";
        WalletEvent walletEvent = walletEventService.createWalletEvent(event, user, value);
        walletEventService.saveWalletEvent(walletEvent);
        user.setWalletBalance(user.getWalletBalance().add(value));
        this.saveUser(user);
    }

    /**
     * Decreases user walletBalance when coupon is closed.
     *
     * @param user
     * @param coupon
     */
    public void walletPlaceBetsWithCouponClosed(User user, Coupon coupon) {
        for (Bet bet : coupon.getBets()) {
            String event = "Bet placed";
            WalletEvent walletEvent = walletEventService.createWalletEvent(event, user, bet.getStake());
            walletEventService.saveWalletEvent(walletEvent);
            user.setWalletBalance(user.getWalletBalance().subtract(bet.getStake()));
        }
        this.saveUser(user);
    }

    public boolean hasEnoughWalletBalance(User user, Coupon coupon, Bet bet) {
        BigDecimal walletBalance = user.getWalletBalance();
        List<Bet> bets = coupon.getBets();
        BigDecimal stake = bet.getStake();
        if(walletBalance==null){
            return false;
        }
        int check = 1;
        if (bets != null && stake != null) {
            if (bets.size() < 1 && stake.compareTo(walletBalance) < 1) {
                return true;
            }
            BigDecimal currentCouponValue = coupon.getBets().stream().map((x) -> x.getStake())
                    .reduce((x, y) -> x.add(y)).get().add(bet.getStake());
            check = walletBalance.compareTo(currentCouponValue);
        }
        return check > 0;
    }

    /**
     * Returns list of friends whom a user chatted with.
     *
     * @param user
     * @param messages
     * @return list of friends whom a user chatted with.
     */
    public List<User> getMessagedFriends(User user, List<Message> messages) {
        Set<User> set1 = messages.stream().map(Message::getFromUser).collect(Collectors.toSet());
        Set<User> set2 = messages.stream().map(Message::getToUser).collect(Collectors.toSet());
        set1.addAll(set2);
        List<User> messFriends = new ArrayList<>(set1);
        messFriends.remove(user);
        return messFriends;
    }

    public void friendAdd(User loggedUser, User friendToAdd) {
        List<User> friends = loggedUser.getFriends();
        if (!friends.contains(friendToAdd)) {
            friends.add(friendToAdd);
            loggedUser.setFriends(friends);
            saveUser(loggedUser);
        }
    }

    public void friendRemove(User loggedUser, User friendToRemove) {
        List<User> friends = loggedUser.getFriends();
        friends.remove(friendToRemove);
        loggedUser.setFriends(friends);
        saveUser(loggedUser);
    }

}