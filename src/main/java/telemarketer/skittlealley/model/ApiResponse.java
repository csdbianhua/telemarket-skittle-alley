package telemarketer.skittlealley.model;

import java.util.Objects;

/**
 * Be careful.
 * Author: Hanson
 * Email: imyijie@outlook.com
 * Date: 2016/12/28
 */
public class ApiResponse {
    private Integer code;
    private Object data;

    public static ApiResponse error(String msg) {
        ApiResponse response = new ApiResponse();
        response.setCode(-1);
        response.setData(msg);
        return response;
    }

    public static ApiResponse code(int code, Object o) {
        ApiResponse response = new ApiResponse();
        response.setCode(code);
        response.setData(o);
        return response;
    }

    public static ApiResponse ok() {
        ApiResponse response = new ApiResponse();
        response.setCode(0);
        return response;
    }

    public static ApiResponse ok(Object object) {
        ApiResponse response = new ApiResponse();
        response.setCode(0);
        response.setData(object);
        return response;
    }

    public Integer getCode() {
        return code;
    }

    public ApiResponse setCode(Integer code) {
        this.code = code;
        return this;
    }

    public Object getData() {
        return data;
    }

    public ApiResponse setData(Object data) {
        this.data = data;
        return this;
    }

    public boolean empty() {
        return Objects.isNull(code);
    }

    public void clear() {
        this.data = null;
        this.code = null;
    }
}
