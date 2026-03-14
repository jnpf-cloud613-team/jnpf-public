package jnpf.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:49
 */
@Data
public class LoginForm {
    @Schema(description = "账号")
    private String account;
    @Schema(description = "密码")
    private String password;

    /**
     * 登录类型
     */
    @Schema(description = "登录类型")
    private String grantType;
    /**
     * 验证码标识
     */
    @Schema(description = "验证码标识")
    private String timestamp;
    /**
     * 来源类型
     */
    private String origin;
    /**
     * 验证码
     */
    @Schema(description = "验证码")
    private String code;

    public LoginForm() {
    }

    public LoginForm(String account, String password) {
        this.account = account;
        this.password = password;
    }
}
