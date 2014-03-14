package soccer;

public class Flag extends FieldObject{
	char name;
        byte props;
	boolean left;
	boolean right;
	boolean top;
	boolean bottom;
	boolean goal;
	boolean penalty;
	boolean center;
	int fromCenter;
	
	public Flag(char name){
		this.name = name;
                if(name == 'g'){
                    goal = true;
                }
	}
	
	public void set(String property) {
		// TODO Auto-generated method stub
		switch(property){
			case("l"):left=true;
			break;
			case("r"):right = true;
			break;
			case("b"):bottom=true;
			break;
			case("t"):top=true;
			break;
			case("g"):goal=true;
			break;
			case("p"):penalty=true;
			break;
			case("c"):center=true;
			break;
		}
	}
	
}
