package com.kaixin.app;

import com.kaixin.core.app.KxConsts;
import com.kaixin.core.db.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

/**
 * Created by zhongshu on 2018/4/27.
 */
@Path("/api/topgoods")
@Produces(value = MediaType.APPLICATION_JSON)
public class TopGoodsApi {

    @GET
    @Path("/comments")
    @Transactional
    public List<Map<String,Object>> Comments(@PathParam("cat") long cat) throws Exception {
        return null;
    }
}
