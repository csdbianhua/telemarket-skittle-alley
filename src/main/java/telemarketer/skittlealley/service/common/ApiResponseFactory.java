package telemarketer.skittlealley.service.common;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.springframework.stereotype.Component;
import telemarketer.skittlealley.model.ApiResponse;

/**
 * Be careful.
 * Author: Hanson
 * Email: imyijie@outlook.com
 * Date: 2017/2/7
 */
@Component
public class ApiResponseFactory extends BasePooledObjectFactory<ApiResponse> {

    @Override
    public ApiResponse create() throws Exception {
        return new ApiResponse();
    }

    @Override
    public PooledObject<ApiResponse> wrap(ApiResponse response) {
        return new DefaultPooledObject<>(response);
    }

    @Override
    public void passivateObject(PooledObject<ApiResponse> p) throws Exception {
        p.getObject().clear();
    }
}
