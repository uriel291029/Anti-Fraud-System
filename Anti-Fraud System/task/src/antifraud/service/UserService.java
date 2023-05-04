package antifraud.service;

import antifraud.domain.antifraud.AccessOperation;
import antifraud.domain.exception.BadRequestException;
import antifraud.domain.exception.ConflictException;
import antifraud.domain.exception.NotFoundException;
import antifraud.domain.request.AccessRequest;
import antifraud.domain.request.RoleRequest;
import antifraud.domain.request.UserRequest;
import antifraud.domain.response.UserResponse;
import antifraud.domain.security.RoleEnum;
import antifraud.model.User;
import antifraud.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  public UserResponse registerUser(UserRequest userRequest) {
    boolean existsByRole = userRepository.existsByRole(RoleEnum.ADMINISTRATOR);
    RoleEnum roleEnum = existsByRole
        ? RoleEnum.MERCHANT : RoleEnum.ADMINISTRATOR;
    boolean unlock = !existsByRole;

    boolean existsByUsername = userRepository.existsByUsername(userRequest.getUsername());
    if (existsByUsername) {
      throw new ConflictException("This is username is used by another user.");
    }

    User user = User.builder()
        .name(userRequest.getName())
        .username(userRequest.getUsername())
        .password(passwordEncoder.encode(userRequest.getPassword()))
        .role(roleEnum)
        .unlock(unlock)
        .build();
    user = userRepository.save(user);

    return UserResponse.builder()
        .id(user.getUserId())
        .username(user.getUsername())
        .name(user.getName())
        .role(roleEnum)
        .build();
  }

  public List<UserResponse> getUsers() {
    return userRepository.findAll(Sort.by(Direction.ASC, "userId")).stream()
        .map(user -> UserResponse.builder()
            .id(user.getUserId())
            .username(user.getUsername())
            .name(user.getName())
            .role(user.getRole())
            .build()).collect(Collectors.toList());
  }

  public UserResponse updateUser(RoleRequest roleRequest) {
    Optional<User> currentUser = Optional.ofNullable(
        userRepository.findByUsername(roleRequest.getUsername())
            .orElseThrow(() -> new NotFoundException("User not found.")));

    if (roleRequest.getRole().equals(RoleEnum.ADMINISTRATOR)) {
      throw new BadRequestException("Bad request");
    }

    if (roleRequest.getRole().equals(currentUser.get().getRole())) {
      throw new ConflictException("Conflict Exception");
    }
    User user = currentUser.get();
    user.setRole(roleRequest.getRole());

    user = userRepository.save(user);

    return UserResponse.builder()
        .id(user.getUserId())
        .username(user.getUsername())
        .name(user.getName())
        .role(user.getRole())
        .build();
  }

  public UserResponse accessUser(AccessRequest accessRequest) {
    Optional<User> currentUser = Optional.ofNullable(
        userRepository.findByUsername(accessRequest.getUsername())
            .orElseThrow(() -> new NotFoundException("User not found.")));

    User user = currentUser.get();
    if (user.getRole().equals(RoleEnum.ADMINISTRATOR)) {
      throw new BadRequestException("Bad request");
    }
    user.setUnlock(accessRequest.getOperation().equals(AccessOperation.UNLOCK));
    userRepository.save(user);
    return UserResponse.builder()
        .status(String.format("User %s %s!", user.getUsername(), user.isUnlock() ? "unlocked" : "locked"))
        .build();
  }

  public UserResponse deleteUser(String username) {
    boolean existsByUsername = userRepository.existsByUsername(username);
    if (!existsByUsername) {
      throw new NotFoundException("This is username has not been found.");
    }

    userRepository.deleteByUsername(username);
    return UserResponse.builder()
        .username(username)
        .status("Deleted successfully!").build();
  }
}

