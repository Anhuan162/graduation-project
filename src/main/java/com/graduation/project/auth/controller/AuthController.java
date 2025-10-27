package com.graduation.project.auth.controller;

import com.graduation.project.auth.dto.request.AuthenticationRequest;
import com.graduation.project.auth.dto.request.IntrospectRequest;
import com.graduation.project.auth.dto.response.ApiResponse;
import com.graduation.project.auth.dto.response.AuthenticationResponse;
import com.graduation.project.auth.dto.response.IntrospectResponse;
import com.graduation.project.auth.dto.response.RefreshTokenResponse;
import com.graduation.project.auth.service.AuthService;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Các API phục vụ xác thực, cấp và thu hồi token.")
public class AuthController {

  private final AuthService authService;


  @Operation(
          summary = "Đăng nhập hệ thống",
          description = """
            API dùng để đăng nhập bằng thông tin tài khoản người dùng (email, mật khẩu).
            Nếu xác thực thành công, trả về `accessToken` và `refreshToken`.
            """
  )
  @ApiResponses({
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
                  responseCode = "200",
                  description = "Đăng nhập thành công",
                  content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))
          ),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
                  responseCode = "401",
                  description = "Sai thông tin đăng nhập"
          ),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
                  responseCode = "400",
                  description = "Yêu cầu không hợp lệ"
          )
  })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
          required = true,
          description = "Thông tin đăng nhập của người dùng (email và password)",
          content = @Content(
                  schema = @Schema(implementation = AuthenticationRequest.class),
                  examples = {
                          @ExampleObject(
                                  name = "Ví dụ đăng nhập",
                                  value = """
                {
                  "email": "admin",
                  "password": "123456"
                }
                """
                          )
                  }
          )
  )
  @PostMapping("/login")
  public ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
    return ApiResponse.<AuthenticationResponse>builder().result(authService.login(request)).build();
  }

  @Operation(
          summary = "Làm mới access token",
          description = """
            API dùng để tạo lại `accessToken` mới từ `refreshToken`.
            Hệ thống sẽ tự động lấy `refreshToken` từ cookie của request.
            Nếu token hợp lệ và chưa hết hạn, sẽ cấp mới `accessToken`.
            """
  )
  @ApiResponses({
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
                  responseCode = "200",
                  description = "Làm mới token thành công",
                  content = @Content(schema = @Schema(implementation = RefreshTokenResponse.class))
          ),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
                  responseCode = "401",
                  description = "Refresh token không hợp lệ hoặc đã hết hạn"
          )
  })
  @PostMapping("/refresh")
  public ApiResponse<RefreshTokenResponse> refresh(HttpServletRequest request)
      throws ParseException, JOSEException {

    String refreshToken = null;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("refreshToken".equals(cookie.getName())) {
          refreshToken = cookie.getValue();
        }
      }
    }

    return ApiResponse.<RefreshTokenResponse>builder()
        .result(authService.refreshToken(refreshToken))
        .build();
  }

  @Operation(
          summary = "Đăng xuất",
          description = """
            API dùng để đăng xuất khỏi hệ thống.
            Khi gọi, hệ thống sẽ thu hồi `refreshToken` khỏi danh sách hợp lệ.
            Sau khi logout, người dùng cần đăng nhập lại để lấy token mới.
            """
  )
  @ApiResponses({
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
                  responseCode = "200",
                  description = "Đăng xuất thành công"
          ),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
                  responseCode = "400",
                  description = "Thiếu hoặc token không hợp lệ"
          )
  })
  @PostMapping("/logout")
  public ApiResponse<Void> logout(@RequestBody Map<String, String> body)
      throws ParseException, JOSEException {
    String refreshToken = body.get("refreshToken");
    authService.logout(refreshToken);
    return ApiResponse.<Void>builder().result(null).build();
  }

  @Operation(
          summary = "Kiểm tra tính hợp lệ của token (Introspect)",
          description = """
            API dùng để kiểm tra `accessToken` có hợp lệ hay không.
            Hệ thống trả về thông tin trạng thái token, bao gồm cả thời gian hết hạn.
            """
  )
  @ApiResponses({
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
                  responseCode = "200",
                  description = "Trả về trạng thái token",
                  content = @Content(schema = @Schema(implementation = IntrospectResponse.class))
          ),
          @io.swagger.v3.oas.annotations.responses.ApiResponse(
                  responseCode = "400",
                  description = "Token không hợp lệ hoặc định dạng sai"
          )
  })
  @PostMapping("/introspect")
  ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
      throws ParseException, JOSEException {
    var result = authService.introspect(request);
    return ApiResponse.<IntrospectResponse>builder().result(result).build();
  }
}
