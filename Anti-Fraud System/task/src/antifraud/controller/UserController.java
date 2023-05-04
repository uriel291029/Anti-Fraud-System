package antifraud.controller;

import antifraud.domain.request.AccessRequest;
import antifraud.domain.request.RoleRequest;
import antifraud.domain.request.UserRequest;
import antifraud.domain.response.UserResponse;
import antifraud.service.UserService;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/auth")
public class UserController {

  private final UserService userService;

  @PostMapping("user")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse registerUser(@Valid @RequestBody UserRequest userRequest) {
    return userService.registerUser(userRequest);
  }

  @RolesAllowed({"ROLE_ADMINISTRATOR", "ROLE_SUPPORT"})
  @GetMapping("list")
  public List<UserResponse> getUsers() {
    return userService.getUsers();
  }

  @RolesAllowed({"ROLE_ADMINISTRATOR"})
  @PutMapping("role")
  public UserResponse updateUser(@Valid @RequestBody RoleRequest roleRequest) {
    return userService.updateUser(roleRequest);
  }

  @RolesAllowed({"ROLE_ADMINISTRATOR"})
  @PutMapping("access")
  public UserResponse accessUser(@Valid @RequestBody AccessRequest accessRequest) {
    return userService.accessUser(accessRequest);
  }

  @RolesAllowed({"ROLE_ADMINISTRATOR"})
  @DeleteMapping("user/{username}")
  public UserResponse removeUser(@PathVariable String username) {
    return userService.deleteUser(username);
  }

  @RolesAllowed({"ROLE_ADMINISTRATOR"})
  @DeleteMapping(value = {"user"})
  public UserResponse removeUserRequestParam(@RequestParam(required = false) String username) {
    return userService.deleteUser(username);
  }
}
