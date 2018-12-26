public class TutorialController extends Controller {

    public SpringObject object;

    ComposedSpringObject cso;

    /* These are the agents senses (inputs) */
	DoubleFeature x; /* Positions */
	DoubleFeature y;
	DoubleFeature vx; /* Velocities */
	DoubleFeature vy;
	DoubleFeature angle; /* Angle */

    /* Example:
     * x.getValue() returns the vertical position of the rocket 
     */

	/* These are the agents actuators (outputs)*/
	RocketEngine leftRocket;
	RocketEngine middleRocket;
	RocketEngine rightRocket;

    /* Example:
     * leftRocket.setBursting(true) turns on the left rocket 
     */
	
	public void init() {
		cso = (ComposedSpringObject) object;
		x = (DoubleFeature) cso.getObjectById("x");
		y = (DoubleFeature) cso.getObjectById("y");
		vx = (DoubleFeature) cso.getObjectById("vx");
		vy = (DoubleFeature) cso.getObjectById("vy");
		angle = (DoubleFeature) cso.getObjectById("angle");

		leftRocket = (RocketEngine) cso.getObjectById("rocket_engine_left");
		rightRocket = (RocketEngine) cso.getObjectById("rocket_engine_right");
		middleRocket = (RocketEngine) cso.getObjectById("rocket_engine_middle");
	}

    public void tick(int currentTime) {

    	/* TODO: Insert your code here */
    	
    	
//    	System.out.println("-----------");
//    	System.out.println(y.getValue());
//    	System.out.println(vx.getValue());
//    	System.out.println(vy.getValue());
//    	System.out.println(angle.getValue());
    	
    	if(vy.getValue() > 1 && y.getValue() > 0) {
    		leftRocket.setBursting(true);
    		middleRocket.setBursting(true);
    		rightRocket.setBursting(true);

    	} else if (vy.getValue() <= 1 && y.getValue() <= 0) {
    		middleRocket.setBursting(false);
    		leftRocket.setBursting(false);
    		rightRocket.setBursting(false);

    	}
    	
    }

}
