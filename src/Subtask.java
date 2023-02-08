public class Subtask extends Task {
    public transient  Epic Parent;

    Subtask(String title, String description, int id, Epic parent){
        
        super(title, description, id);
        this.Parent = parent;
    }

    public Subtask CopyTask(){
        
        Subtask task = new Subtask(Title, Description, Id, Parent);
        task.Status = this.Status;
        return task;
    }
    public String toString(){
        return "Subtask " + Integer.toString(Id);
    }
}
