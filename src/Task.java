public class Task {
    public String Title;
    public String Description;
    public int Id;
    public TaskStatus Status = TaskStatus.NEW;

    Task(String title, String description, int id){
        this.Title = title;
        this.Description = description;
        this.Id = id;
    }

    public Task CopyTask(){
        Task task = new Task(Title, Description, Id);
        task.Status = this.Status;
        return task;
    }

    public String toString(){
        return "Task " + Integer.toString(Id);
    }
}
