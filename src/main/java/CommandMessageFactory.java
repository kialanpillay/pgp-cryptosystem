public class CommandMessageFactory {

    public CommandMessage getCommandMessage(String type, String message){
        if(type == null){
            return null;
        }
        if (type.equalsIgnoreCase("QUIT")){
            return new QuitMessage(message);
        }

        else if(type.equalsIgnoreCase("AUTH")){
            return new AuthenticateMessage(message);
        }

        return null;
    }
}
