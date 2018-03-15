var app = (function () {

    class Point{
        constructor(x,y){
            this.x=x;
            this.y=y;
        }        
    }
    
    var stompClient = null;
    var currentBoard =null;

    var addPointToCanvas = function (point) {        
        var canvas = document.getElementById("canvas");
        var ctx = canvas.getContext("2d");
        ctx.beginPath();
        ctx.arc(point.x, point.y, 3, 0, 2 * Math.PI);
        ctx.stroke();
    };

    var addPolygonToCanvas = function(points){
        var canvas = document.getElementById("canvas");
        var ctx = canvas.getContext("2d");
        ctx.beginPath();
        ctx.moveTo(points[0].x,points[0].y);
        for(var i=1; i<points.length; i++){
            ctx.lineTo(points[i].x,points[i].y);
            console.log(points[i]);
        }
        ctx.closePath();
        ctx.fill();
        ctx.stroke();
    }
    
    
    var getMousePosition = function (evt) {
        canvas = document.getElementById("canvas");
        var rect = canvas.getBoundingClientRect();
        return {
            x: evt.clientX - rect.left,
            y: evt.clientY - rect.top
        };
    };


    var connectAndSubscribe = function (board) {
        console.info('Connecting to WS...');
        var socket = new SockJS('/stompendpoint');
        stompClient = Stomp.over(socket);
        currentBoard = board;
        
        //subscribe to /topic/newpoint when connections succeed
        stompClient.connect({}, function (frame) {
            console.log('Connected: ' + frame);
            stompClient.subscribe('/topic/newpoint.' + board , function (eventbody) {
            var punto=JSON.parse(eventbody.body);
            addPointToCanvas(punto);
            });
            
            stompClient.subscribe('/topic/newpolygon.' + board , function (eventbody) {
            var points = JSON.parse(eventbody.body);
            addPolygonToCanvas(points);
            });
        });

    };
    
    

    return {

        init: function (board) {
            var can = document.getElementById("canvas");
            
            //websocket connection
            connectAndSubscribe(board);
            alert("Connected to board "+ board);
        },

        publishPoint: function(event){
            var pos = getMousePosition(event);
            var px = pos.x;
            var py = pos.y;
            var pt=new Point(px,py);
            console.info("publishing point at "+pt);
            addPointToCanvas(pt);
            //publicar el evento
            stompClient.send("/app/newpoint."+ currentBoard, {}, JSON.stringify(pt));
            //alert(JSON.stringify(pt));
            

        },

        disconnect: function () {
            if (stompClient !== null) {
                stompClient.disconnect();
            }
            setConnected(false);
            console.log("Disconnected");
        }
    };

})();