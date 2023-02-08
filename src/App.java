import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.HashMap;

import com.sun.net.httpserver.HttpServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;;


public class App {
    private static TaskManager manager;
    public static void main(String[] args) throws Exception {
        manager = new TaskManager();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        manager.CreateNewTask(new Task("title", "null", manager.GenerateId()));
        System.out.println(gson.toJson(manager.GetTaskById(1)));
        manager.ConvertTaskToEpic(manager.GetTaskById(1));
        System.out.println(gson.toJson(manager.GetTaskById(1)));
        manager.CreateNewTask((Task)(new Subtask("subtask", "null", manager.GenerateId(), (Epic)manager.GetTaskById(1))));
        manager.CreateNewTask((Task)new Subtask("subtask", "null", manager.GenerateId(), (Epic)manager.GetTaskById(1)));
        System.out.println(gson.toJson(manager.GetTaskById(1)));
        
        int serverPort = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);

        //Создание нового таска
        server.createContext("/api/task/new", (exchange -> {

            Map <String,String> query = queryToMap(exchange.getRequestURI().getQuery());

            
            String title = query.get("title") == null ? "Title" : (String)query.get("title");
            String description = query.get("description") == null ? "Description" : (String)query.get("description");
            int parent = query.get("parent") == null ? -1 : Integer.parseInt(query.get("parent"));

            Task task;


            if(parent == -1){
                //Таск
                System.out.println("Create new task");
                task = new Task(title, description, manager.GenerateId());
            }else{
                //сабтаск
                System.out.println("Create new subtask of " + parent);
                Task parentTask = manager.GetTaskById(parent);
                if(!(parentTask instanceof Epic)) manager.ConvertTaskToEpic(parentTask);
                task = (Task)(new Subtask(title, description, manager.GenerateId(), (Epic)manager.GetTaskById(parent)));
            }

            manager.CreateNewTask(task);
            String respText = "New " + (parent == -1 ? "task" : "subtask") + " added, id: " + task.Id;
            exchange.sendResponseHeaders(200, respText.getBytes().length);
            OutputStream output = exchange.getResponseBody();
            output.write(respText.getBytes());
            output.flush();
            exchange.close();
        }));
        server.createContext("/api/task/all", (exchange -> {

            Task[] tasks = manager.GetAll();
            String respText = gson.toJson(tasks);
            exchange.sendResponseHeaders(200, respText.getBytes().length);
            OutputStream output = exchange.getResponseBody();
            output.write(respText.getBytes());
            output.flush();
            exchange.close();
        }));
        server.createContext("/api/epic/all", (exchange -> {
            Epic[] tasks = manager.GetAllEpics();
            String respText = gson.toJson(tasks);
            exchange.sendResponseHeaders(200, respText.getBytes().length);
            OutputStream output = exchange.getResponseBody();
            output.write(respText.getBytes());
            output.flush();
            exchange.close();
        }));
        server.createContext("/api/subtask/all", (exchange -> {
            Subtask[] tasks = manager.GetAllSubtasks();
            
            System.out.println(gson.toJson(manager.GetTaskById(1)));
            String respText = gson.toJson(tasks);
            exchange.sendResponseHeaders(200, respText.getBytes().length);
            OutputStream output = exchange.getResponseBody();
            output.write(respText.getBytes());
            output.flush();
            exchange.close();
        }));
        server.createContext("/api/task/", (exchange -> {
            String[] path = exchange.getRequestURI().getPath().split("/");

            switch(path.length){
                case 4:{
                    try{
                        Task tasks = manager.GetTaskById(Integer.parseInt(path[3]));
                        String respText = gson.toJson(tasks);
                        exchange.sendResponseHeaders(200, respText.getBytes().length);
                        OutputStream output = exchange.getResponseBody();
                        output.write(respText.getBytes());
                        output.flush();
                        exchange.close();
                    }catch(NumberFormatException e){
                        String respText = "task id incorrect";
                        exchange.sendResponseHeaders(200, respText.getBytes().length);
                        OutputStream output = exchange.getResponseBody();
                        output.write(respText.getBytes());
                        output.flush();
                        exchange.close();
                    }
                }
                case 5:{
                    System.out.println(path[4]);
                    try{
                        switch(path[4]){
                            case "edit":{
                                Map <String,String> query = queryToMap(exchange.getRequestURI().getQuery());
                
                                int id = Integer.parseInt(path[3]);
                               
                                Task t = manager.GetTaskById(id);

                                t.Title = query.get("title") == null ? t.Title : (String)query.get("title");
                                t.Description = query.get("description") == null ? t.Description : (String)query.get("description");
                                int parent = query.get("parent") == null ? -1 : Integer.parseInt(query.get("parent"));
                                boolean statusParam = true;
                
                                switch(query.get("status") == null ? "" : query.get("status")){
                                    case "in_progress":{
                                        t.Status = TaskStatus.IN_PROGRESS;
                                        break;
                                    }
                                    case "done":{
                                        t.Status = TaskStatus.DONE;
                                        break;
                                    }
                                    default:{
                                        statusParam = false;
                                        break;}
                                }
                                if(parent != -1 || !(manager.GetTaskById(id) instanceof Subtask)){
                                    //сабтаск
                                    System.out.println("Edit subtask of " + parent);
                                    Task parentTask = parent == - 1 ? ((Subtask)t).Parent : manager.GetTaskById(parent);
                                    System.out.println(!(parentTask instanceof Epic));
                                    if(!(parentTask instanceof Epic)) manager.ConvertTaskToEpic(parentTask);
                                    System.out.println("test");
                                    ((Subtask)t).Parent = query.get("parent") == null ? ((Subtask)t).Parent : (Epic)parentTask;
                
                                    if(statusParam){
                                        ((Epic)parentTask).CheckStatus();
                                    }
                                }
                
                                String respText = gson.toJson(t);
                                exchange.sendResponseHeaders(200, respText.getBytes().length);
                                OutputStream output = exchange.getResponseBody();
                                output.write(respText.getBytes());
                                output.flush();
                                exchange.close();
                                break;
                            }
                            case "delete":{
                                int id = Integer.parseInt(path[3]);
                                manager.RemoveTask(id);

                                String respText = "Task " + id + " remove with all child tasks";
                                exchange.sendResponseHeaders(200, respText.getBytes().length);
                                OutputStream output = exchange.getResponseBody();
                                output.write(respText.getBytes());
                                output.flush();
                                exchange.close();
                                break;
                            }
                            default:{
                                String respText = "api endpoint not found";
                                exchange.sendResponseHeaders(200, respText.getBytes().length);
                                OutputStream output = exchange.getResponseBody();
                                output.write(respText.getBytes());
                                output.flush();
                                exchange.close();
                                break;
                            }
                        }
                    }catch(NumberFormatException e){
                        String respText = "task id incorrect";
                        exchange.sendResponseHeaders(200, respText.getBytes().length);
                        OutputStream output = exchange.getResponseBody();
                        output.write(respText.getBytes());
                        output.flush();
                        exchange.close();
                    }
                }
            }
        }));
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    public static Map<String, String> queryToMap(String query){

        if(query == null) query = "";
        Map<String, String> result = new HashMap<String, String>();
    
        for (String param : query.split("&")) {
    
            String pair[] = param.split("=");
    
            if (pair.length>1) {
    
                result.put(pair[0], pair[1]);
    
            }else{
    
                result.put(pair[0], "");
    
            }
    
        }
    
        return result;
    
      }

    public static String[] splitPath(String path){
        return path.split("/");
    }

}
