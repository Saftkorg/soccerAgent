package soccer;

public class Flag {
	char name;
	boolean left;
	boolean right;
	boolean top;
	boolean bottom;
	boolean goal;
	boolean penalty;
	boolean center;
	int fromCenter;
	int distance;
	int degree;
	int distanceChange;
	int degreeChange;
	
	public Flag(char name){
		this.name = name;
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
