package tddc17;

import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.*;

class MyAgentState {
    public int[][] world = new int[30][30];
    public int initialized = 0;
    final int UNKNOWN = 0;
    final int WALL = 1;
    final int CLEAR = 2;
    final int DIRT = 3;
    final int HOME = 4;
    final int ACTION_NONE = 0;
    final int ACTION_MOVE_FORWARD = 1;
    final int ACTION_TURN_RIGHT = 2;
    final int ACTION_TURN_LEFT = 3;
    final int ACTION_SUCK = 4;

    public int agent_x_position = 1;
    public int agent_y_position = 1;
    public int agent_last_action = ACTION_NONE;

    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;
    public int agent_direction = EAST;

    MyAgentState() {
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world[i].length; j++) {
                world[i][j] = UNKNOWN;
            }
        }
        // Hemma
        world[1][1] = HOME;
        agent_last_action = ACTION_NONE;
    }

    // Based on the last action and the received percept updates the x & y agent
    // position
    public void updatePosition(DynamicPercept p) {
        Boolean bump = (Boolean) p.getAttribute("bump");

        if (agent_last_action == ACTION_MOVE_FORWARD && !bump) {
            switch (agent_direction) {
            case MyAgentState.NORTH:
                agent_y_position--;
                break;
            case MyAgentState.EAST:
                agent_x_position++;
                break;
            case MyAgentState.SOUTH:
                agent_y_position++;
                break;
            case MyAgentState.WEST:
                agent_x_position--;
                break;
            }
        }

    }

    public void updateWorld(int x_position, int y_position, int info) {
        world[x_position][y_position] = info;
    }

    public void printWorldDebug() {
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world[i].length; j++) {
                if (world[j][i] == UNKNOWN)
                    System.out.print("? ");
                if (world[j][i] == WALL)
                    System.out.print(" # ");
                if (world[j][i] == CLEAR)
                    System.out.print(" . ");
                if (world[j][i] == DIRT)
                    System.out.print(" D ");
                if (world[j][i] == HOME)
                    System.out.print(" H ");
            }
            System.out.println("");
        }
    }
}

class MyAgentProgram implements AgentProgram {

    // CHANGED THIS
    private int initnialRandomActions = 10;
    private Random random_generator = new Random();

    // Here you can define your variables!
    public int iterationCounter = 1000;
    public MyAgentState state = new MyAgentState();

    public Tile[][] board = new Tile[state.world.length + 5][state.world.length + 5];
    public Stack<Tile> toVisit = new Stack<>();
    public Stack<Tile> backtrackStack = new Stack<>();
    public int targetDir;
    public boolean otherWay = false;
    public boolean goingHome = false;
    public Tile[] lastBumps = new Tile[20];
    public int counter = 0;
    public boolean backtracking = false;

    // moves the Agent to a random start position
    // uses percepts to update the Agent position - only the position, other
    // percepts are ignored
    // returns a random action

    private Action moveToRandomStartPosition(DynamicPercept percept) {
        System.out.println("RANDOM");
        int action = random_generator.nextInt(6);
        initnialRandomActions--;
        state.updatePosition(percept);
        if (action == 0) {
            state.agent_direction = ((state.agent_direction - 1) % 4);
            if (state.agent_direction < 0) {
                state.agent_direction += 4;
            }
            state.agent_last_action = state.ACTION_TURN_LEFT;
            return LIUVacuumEnvironment.ACTION_TURN_LEFT;
        } else if (action == 1) {
            state.agent_direction = ((state.agent_direction + 1) % 4);
            state.agent_last_action = state.ACTION_TURN_RIGHT;
            return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
        }
        state.agent_last_action = state.ACTION_MOVE_FORWARD;
        return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
    }

    @Override
    public Action execute(Percept percept) {

        System.out.println("---------------------------------------------");

        // DO NOT REMOVE this if condition!!!
        if (initnialRandomActions > 0) {
            return moveToRandomStartPosition((DynamicPercept) percept);
        } else if (initnialRandomActions == 0) {
            // process percept for the last step of the initial random actions
            initnialRandomActions--;
            state.updatePosition((DynamicPercept) percept);
            System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
            state.agent_last_action = state.ACTION_SUCK;

            // Create board for memory
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    board[i][j] = new Tile(i, j);
                }
            }
            return LIUVacuumEnvironment.ACTION_SUCK;
        }

        // This example agent program will update the internal agent state while only
        // moving forward.
        // START HERE - code below should be modified!

        System.out.println("x=" + state.agent_x_position);
        System.out.println("y=" + state.agent_y_position);
        System.out.println("dir=" + state.agent_direction);

        iterationCounter--;

        // if (iterationCounter==0) {
        // System.out.println("Iteration counter is 0");
        // return NoOpAction.NO_OP;
        // }

        DynamicPercept p = (DynamicPercept) percept;
        Boolean bump = (Boolean) p.getAttribute("bump");
        Boolean dirt = (Boolean) p.getAttribute("dirt");
        Boolean home = (Boolean) p.getAttribute("home");
        System.out.println("percept: " + p);

        // State update based on the percept value and the last action
        state.updatePosition((DynamicPercept) percept);

        // Updates the Map with bumped wall
        if (bump) {
            switch (state.agent_direction) {
            case MyAgentState.NORTH:
                board[state.agent_x_position][state.agent_y_position - 1].wall = true;
                state.updateWorld(state.agent_x_position, state.agent_y_position - 1, state.WALL);
                break;
            case MyAgentState.EAST:
                board[state.agent_x_position + 1][state.agent_y_position].wall = true;
                state.updateWorld(state.agent_x_position + 1, state.agent_y_position, state.WALL);
                break;
            case MyAgentState.SOUTH:
                board[state.agent_x_position][state.agent_y_position + 1].wall = true;
                state.updateWorld(state.agent_x_position, state.agent_y_position + 1, state.WALL);
                break;
            case MyAgentState.WEST:
                board[state.agent_x_position - 1][state.agent_y_position].wall = true;
                state.updateWorld(state.agent_x_position - 1, state.agent_y_position, state.WALL);
                break;
            }
        }
        if (dirt) {
            state.updateWorld(state.agent_x_position, state.agent_y_position, state.DIRT);
        } else {
            state.updateWorld(state.agent_x_position, state.agent_y_position, state.CLEAR);
        }

        state.printWorldDebug();

        ////////
        // if(!toVisit.empty()){
        // System.out.println("toVisit contains:");
        // for(Tile t : toVisit) {
        // System.out.println(t.x + "," + t.y);
        // }
        //
        // System.out.println("Going to: " + toVisit.peek().x+","+toVisit.peek().y);
        // System.out.println("-------");
        // }
        ////////

        // Next action selection based on the percept value
        if (dirt) {
            toVisit.pop();
            System.out.println("DIRT -> choosing SUCK action!");
            state.agent_last_action = state.ACTION_SUCK;
            return LIUVacuumEnvironment.ACTION_SUCK;
        }

        Tile current = board[state.agent_x_position][state.agent_y_position];

        if (home) {
            if (goingHome) {
                System.out.println("We made it in " + (1000 - iterationCounter) + "moves");
                // state.printWorldDebug();
                return NoOpAction.NO_OP;
            }
        }
        if (bump) {
            backtrackStack.pop();
            // Keep track of how many times we've bumped
            logBump(current);
            if (checkStuck(current) && !backtracking) {
                System.out.println("We are STUCK!!!");
                return getUnStuck();
            } else
                return handleBump();
        } else {

            // We didn't just bump, so keep going
            Tile targetNode = floodBoard(board[state.agent_x_position][state.agent_y_position]);
            targetDir = getDirection(targetNode);

            // Direction is wrong
            if (state.agent_direction != targetDir) {
                return rotateToFace(targetDir);
            } else {
                // We are moving forward, log previous move
                backtrackStack.push(current);
                // System.out.println("BacktrackStack contains: ");
                // for(Tile t : backtrackStack){
                // System.out.println(t.x + ","+t.y);
                //
                // }

                state.agent_last_action = state.ACTION_MOVE_FORWARD;
                return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
            }

        }
    }

    private Action getUnStuck() {
        backtracking = true;
        Stack<Tile> backTrack = (Stack<Tile>) backtrackStack.clone();
        Stack<Tile> reverse = new Stack<>();
        Tile current = board[state.agent_x_position][state.agent_y_position];
        Tile target = toVisit.peek();
        reverse.push(current);

        boolean done = false;
        while (!done) {
            Tile next = backTrack.peek();
            int dx = target.x - next.x;
            int dy = target.y - next.y;

            // smart backtracking
            while (reverse.contains(next)) {
                reverse.pop();
            }
            reverse.push(backTrack.pop());

            // next tile to visit is adjacent to target tile
            if ((dx == 0 && Math.abs(dy) == 1) || (dy == 0 && Math.abs(dx) == 1)) {
                // System.out.println("We found path to adjacent tile to target");
                // System.out.println("Backtracking according to:");
                // for(Tile t : reverse){
                // System.out.println(t.x+","+t.y);
                // }
                // Set marker to go there
                board[next.x][next.y].goHere = true;
                done = true;
            }
        }

        // Add path to toVisit
        while (!reverse.empty()) {
            toVisit.push(reverse.pop());
        }
        // Pop off the tile we're currently on
        toVisit.pop();

        // Toggles choice of direction
        otherWay = !otherWay;

        targetDir = getDirection(toVisit.peek());
        return rotateToFace(targetDir);
    }

    private void logBump(Tile current) {
        counter++;
        int n = counter % 5;
        lastBumps[n] = current;
    }

    private boolean checkStuck(Tile current) {
        int bumps = 4;
        int n = counter % bumps;

        // Don't check until bump counter is at least 5
        if (counter < bumps + 1) {
            return false;
        } else {
            int x = 0;
            while ((current.x == lastBumps[n].x && current.y == lastBumps[n].y) && x < bumps) {
                n--;
                x++;
                if (n < 0) {
                    n += bumps;
                }
            }
            return x == bumps;
        }
    }

    public Action handleBump() {
        int dx = toVisit.peek().x - state.agent_x_position;
        int dy = toVisit.peek().y - state.agent_y_position;

        // We are adjacent to target square
        if ((dx == 0 && Math.abs(dy) == 1) || (dy == 0 && Math.abs(dx) == 1)) {
            toVisit.pop();
            if (toVisit.empty()) {
                System.out.println("Going home!");
                goHome();
            }
        } else {
            // Toggles direction when there are two options, since we just bumped into a
            // wall
            otherWay = !otherWay;
        }
        targetDir = getDirection(toVisit.peek());
        return rotateToFace(targetDir);
    }

    public Action rotateToFace(int dir) {
        int currentDir = state.agent_direction;
        int action = currentDir - dir;
        if (action < 0) {
            action += 4;
        }
        switch (action) {
        // Target is same direction. This only happens if we are obstructed by a wall
        case 0:
            backtrackStack.push(backtrackStack.peek());
            state.agent_last_action = state.ACTION_MOVE_FORWARD;
            return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
        // Target is to the left
        case 1:
            return turnLeft();
        // Target is behind us
        case 2:
            return turnLeft();
        // Target is to the right
        case 3:
            return turnRight();
        // We are standing on target tile
        default:
            if (goingHome) {
                // We are home
                System.out.println("We must be home!");
                // state.printWorldDebug();
                return NoOpAction.NO_OP;

                // This shouldn't happen
            } else {
                // But if it somehow does, go to the next tile in the stack
                System.out.println("popping toVisit");
                toVisit.pop();
                targetDir = getDirection(toVisit.peek());
                return rotateToFace(targetDir);
            }

        }
    }

    public int getDirection(Tile targetTile) {
        int dx = targetTile.x - state.agent_x_position;
        int dy = targetTile.y - state.agent_y_position;

        if (dx < 0) { // dir = 3
            if (dy < 0) { // dir = 3 || dir = 0
                if (otherWay) {
                    return 0;

                } else
                    return 3;
            } else if (dy > 0) { // dir = 3 || dir = 2
                if (otherWay) {
                    return 2;
                } else
                    return 3;
            } else
                return 3; // dy == 0;

        } else if (dx > 0) { // dir = 1
            if (dy < 0) { // dir = 1 || dir = 0
                if (otherWay) {
                    return 0;
                } else
                    return 1;
            } else if (dy > 0) { // dir = 1 || dir = 2
                if (otherWay) {
                    return 2;
                } else
                    return 1;
            } else
                return 1; // dy == 0;

        } else { // dx == 0
            if (dy < 0) { // dir = 0
                return 0;
            } else if (dy > 0) { // dir = 2
                return 2;
            } else {
                // We are standing on target node
                System.out.println("We are standing on target");
                ;
                return 9;
            }
        }

    }

    public Tile floodBoard(Tile current) {

        // We just visited our current Tile
        if (!current.visited) {
            current.visited = true;
        }

        // Calculate distance to target
        if (!toVisit.empty()) {
            int distance;
            int dx = Math.abs(toVisit.peek().x - state.agent_x_position);
            int dy = Math.abs(toVisit.peek().y - state.agent_y_position);
            distance = dx + dy;

            // If distance > 1 we keep traversing
            if (distance == 0) {
                toVisit.pop();
            }
        }

        // Don't add neighbors if we are backtracking
        if (!backtracking) {
            // Adds neighbors that we haven't visited or that aren't walls
            Stack<Tile> neighbors = new Stack<>();
            neighbors.push(board[current.x][current.y + 1]);
            neighbors.push(board[current.x][current.y - 1]);
            neighbors.push(board[current.x - 1][current.y]);
            neighbors.push(board[current.x + 1][current.y]);
            while (!neighbors.empty()) {
                Tile n = neighbors.pop();
                if (n.visited || n.wall) {
                    neighbors.remove(n);

                } else {
                    toVisit.push(n);
                }
            }
            Stack<Tile> reverse = new Stack<>();
            Stack<Tile> temp = new Stack<>();

            // Reverse stack to keep the Flood Fill algorithm intact
            while (!toVisit.empty()) {
                reverse.push(toVisit.pop());
            }
            // Removes tiles we already added
            for (Tile t : reverse) {
                if (t.visited || t.wall || temp.contains(t)) {
                    // Do nothing
                } else {
                    temp.push(t);
                }
            }
            // Reverse trimmed stack to toVisit
            while (!temp.empty()) {
                toVisit.push(temp.pop());
            }
            // We are backtracking
        } else {
            // We finished backtracking
            if (current.goHere) {
                System.out.println("Done backtracking");
                current.goHere = false;
                backtracking = false;
            }
        }

        // If for some reason last tile in stack isn't a wall tile, and we're standing
        // on it
        if (toVisit.empty()) {
            goHome();
        }

        return toVisit.peek();
    }

    public void goHome() {
        board[1][1].visited = false;
        toVisit.push(board[1][1]);
        goingHome = true;
    }

    private Action turnRight() {
        state.agent_direction = ((state.agent_direction + 1) % 4);
        state.agent_last_action = state.ACTION_TURN_RIGHT;
        return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
    }

    private Action turnLeft() {
        state.agent_direction = ((state.agent_direction - 1) % 4);
        if (state.agent_direction < 0) {
            state.agent_direction += 4;
        }
        state.agent_last_action = state.ACTION_TURN_LEFT;
        return LIUVacuumEnvironment.ACTION_TURN_LEFT;
    }

}

public class MyVacuumAgent extends AbstractAgent {
    public MyVacuumAgent() {
        super(new MyAgentProgram());
    }
}

class Tile {
    public boolean wall;
    public boolean visited;
    public boolean goHere;
    public int x, y;

    Tile(int x, int y) {
        this.x = x;
        this.y = y;
        wall = false;
        visited = false;
        goHere = false;
    }
}
