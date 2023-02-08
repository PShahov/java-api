import java.util.ArrayList;;

public class Epic extends Task {
    public ArrayList<Subtask> Subtasks = new ArrayList<Subtask>();

    Epic(String title, String description, int id){
        super(title, description, id);
    }

    public TaskStatus CheckStatus(){

        boolean p = false;
        boolean d = false;

        for(int i = 0; i < Subtasks.size();i++){
            switch(Subtasks.get(i).Status){
                case IN_PROGRESS:{
                    p = true;
                    break;
                }
                case DONE:{
                    d = true;
                    break;
                }
                default:{break;}
            }
            if(p){
                break;
            }
        }

        if(p){
            Status = TaskStatus.IN_PROGRESS;
        }else if(d){
            Status = TaskStatus.DONE;
        }else{
            Status = TaskStatus.NEW;
        }

        return Status;
    }

    public Epic CopyTask(){
        Epic epic = new Epic(Title, Description, Id);
        epic.Subtasks = this.Subtasks;
        epic.Status = this.Status;
        return epic;
    }
    public String toString(){
        return "Epic " + Integer.toString(Id);
    }
}
