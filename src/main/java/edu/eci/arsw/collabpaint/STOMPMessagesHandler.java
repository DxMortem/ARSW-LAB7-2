package edu.eci.arsw.collabpaint;

import edu.eci.arsw.collabpaint.model.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mort
 */

@Controller
public class STOMPMessagesHandler {
    @Autowired
    SimpMessagingTemplate msgt;

    private ConcurrentHashMap<String,ArrayList<Point>> poligons = new ConcurrentHashMap<String, ArrayList<Point>>();

    @MessageMapping("/newpoint.{numdibujo}")
    public void handlePointEvent(Point pt,@DestinationVariable String numdibujo) throws Exception {
        System.out.println("Nuevo punto recibido en el servidor!:"+pt);
        if (poligons.contains(numdibujo)){
            poligons.get(numdibujo).add(pt);
            if(poligons.get(numdibujo).size()>3){
                msgt.convertAndSend("/topic/newpolygon."+numdibujo, poligons.get(numdibujo));
            }
        }else{
            poligons.put(numdibujo,new ArrayList<Point>());
            poligons.get(numdibujo).add(pt);
        }
        msgt.convertAndSend("/topic/newpoint."+numdibujo, pt);
    }
}
