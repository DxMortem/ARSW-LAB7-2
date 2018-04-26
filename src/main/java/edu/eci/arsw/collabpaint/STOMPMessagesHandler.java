package edu.eci.arsw.collabpaint;

import edu.eci.arsw.collabpaint.model.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import util.JedisUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mort
 */

@Controller
public class STOMPMessagesHandler {
    @Autowired
    SimpMessagingTemplate msgt;



    String luaScript = "local xval,yval; if (redis.call('LLEN','x')==4) then xval=redis.call('LRANGE','x',0,-1); yval=redis.call('LRANGE','y',0,-1); redis.call('DEL','x'); redis.call('DEL','y'); return {xval,yval}; else return {}; end";

    @MessageMapping("/newpoint.{numdibujo}")
    public void handlePointEvent(Point pt,@DestinationVariable String numdibujo) throws Exception {
        Jedis jedis = JedisUtil.getPool().getResource();
        List<Object> res = new ArrayList<Object>();

        while (!res.isEmpty()){
            jedis.watch("x","y");
            Transaction t = jedis.multi();
            t.rpush("x", String.valueOf(pt.getX()));
            t.rpush("y", String.valueOf(pt.getY()));
            Response<Object> luares = t.eval(luaScript.getBytes(),0,"0".getBytes());
            res = t.exec();
            if (((ArrayList) luares.get()).size()==2){
                List<Point> points = new ArrayList<Point>();
                for(int i=0;i<5;i++){
                    points.add(new Point(Integer.parseInt( new String((byte[])((ArrayList)(((ArrayList)luares.get()).get(0))).get(i))),Integer.parseInt( new String((byte[])((ArrayList)(((ArrayList)luares.get()).get(1))).get(i)))));
                }
                msgt.convertAndSend("/topic/newpolygon."+numdibujo, points);
            }
        }
        msgt.convertAndSend("/topic/newpoint."+numdibujo, pt);
        jedis.close();
    }
}
