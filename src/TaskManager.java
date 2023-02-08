import java.util.HashMap;
import java.util.ArrayList;;

public class TaskManager {
    private int currentId = 0;

    private HashMap<Integer, Task> tasks = new HashMap<Integer, Task>();

    public int GenerateId(){
        currentId++;
        return currentId;
    }

    public Task CreateNewTask(Task newTask){

        if(newTask instanceof Subtask){
            //если таск это сабтаск то при необходимости конвертируем родительский таск в эпик, добавляем новый таск в словарь и связываем их
            if(!(((Subtask)newTask).Parent instanceof Task)) this.ConvertTaskToEpic(((Subtask)newTask).Parent);
            
            ((Subtask)newTask).Parent.Subtasks.add((Subtask)newTask);
            tasks.put(newTask.Id, newTask);
        }else{
            //если нет родителя то просто добавляем новый таск в словарь
            tasks.put(newTask.Id, newTask);
        }

        return newTask;
    }

    public Epic ConvertTaskToEpic(Task task){
        if(!(task instanceof Epic) && !(task instanceof Subtask)){
            Epic epic = new Epic(task.Title, task.Description, task.Id);
            tasks.put(task.Id, epic);
            return epic;
        }

        throw new IllegalArgumentException("task parameter must be instance of \"Task\" class!");
    }

    public void EditTask(int original, Task updated){
        this.EditTask((Task)tasks.get(original), updated);
    }

    public void EditTask(Task original, Task updated){
        if(original.Id == updated.Id){
            original = updated;
            if(updated instanceof Subtask){
                ((Subtask)updated).Parent.CheckStatus();
            }
        }
    }

    public Epic[] GetAllEpics(){
        ArrayList<Epic> retVal = new ArrayList<Epic>();

        tasks.forEach((k,v) -> {
            if(v instanceof Epic) retVal.add((Epic)v);
        });

        return (Epic[])(retVal.toArray(new Epic[retVal.size()]));
    }

    public Task[] GetAllTasks(){
        ArrayList<Task> retVal = new ArrayList<Task>();

        tasks.forEach((k,v) -> {
            if(!(v instanceof Epic) && !(v instanceof Subtask)) retVal.add((Task)v);
        });

        return (Task[])(retVal.toArray(new Task[retVal.size()]));
    }

    public Subtask[] GetAllSubtasks(){
        ArrayList<Subtask> retVal = new ArrayList<Subtask>();

        tasks.forEach((k,v) -> {
            if(v instanceof Subtask) retVal.add((Subtask)v);
        });

        return (Subtask[])(retVal.toArray(new Subtask[retVal.size()]));
    }

    public Task[] GetAll(){
        ArrayList<Task> retVal = new ArrayList<Task>();

        tasks.forEach((k,v) -> {
            if(!(v instanceof Subtask)) retVal.add((Task)v);
        });


        return (Task[])(retVal.toArray(new Task[retVal.size()]));
    }

    public Task GetTaskById(int id){
        return (Task)tasks.get(id);
    }

    public void RemoveTask(Task task){
        this.RemoveTask(task.Id);
    }
    public void RemoveTask(int id){
        if(tasks.get(id) instanceof Subtask){
            ((Subtask)tasks.get(id)).Parent.Subtasks.remove(tasks.get(id));
        }else if(tasks.get(id) instanceof Epic){
            Epic epic = (Epic)tasks.get(id);
            for(int i = 0;i < epic.Subtasks.size();i++){
                tasks.remove(epic.Subtasks.get(i).Id);
            }
        }
        tasks.remove(id);
    }
    public void RemoveAll(){
        tasks.clear();
    }

    public Subtask[] GetEpicSubs(int id){
        return (Subtask[])((Epic)tasks.get(id)).Subtasks.toArray();
    }
    public Subtask[] GetEpicSubs(Epic epic){
        return (Subtask[])epic.Subtasks.toArray();
    }
}
