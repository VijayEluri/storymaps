
/* 
    Copyright: (c) 2006-2012 Sean Hammond <seanhammond@seanh.cc>

    This file is part of Storymaps.

    Storymaps is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Storymaps is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Storymaps.  If not, see <http://www.gnu.org/licenses/>.

*/
package storymaps;
import DragAndDrop.DragDropObserver;
import DragAndDrop.Draggable;
import DragAndDrop.DropEvent;
import DragAndDrop.NodeAlreadyDraggableException;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PInterpolatingActivity;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PUtil;
import java.util.logging.Logger;

class StoryCard extends StoryCardBase implements Receiver,
        DragDropObserver, Comparable, Originator {
            
    private boolean highlighted = false;
    private FunctionEditor editor;
    private Draggable draggable;
    
    /**
     * The activity used for both scaling up and scaling down the story card
     * to highlight and unhighlight it.
     */
    private PInterpolatingActivity activity;
    
    public StoryCard(Function function) {
        this(function,"");
    }
    
    public StoryCard(Function function, String text) {

        super(function);
        
        getNode().addAttribute("StoryCard",this);
                                       
        getNode().addInputEventListener(new PBasicInputEventHandler() {
            // Make the story card scale up when the mouse enters it, and down
            // again when the mouse leaves it.
            @Override
            public void mouseEntered(PInputEvent event) {
                StoryCard.this.highlight();
            }
            @Override
            public void mouseExited(PInputEvent event) {
                StoryCard.this.unhighlight();
            }    
            
            @Override
            public void mouseClicked(PInputEvent event) {
                // Disable mouse clicking on story cards.
                /*
                if (event.getButton() == 1 && event.getClickCount() == 2) {
                    // If the StoryCard is double-clicked with the left mouse
                    // button send the "StoryCard double-clicked" message.
                    Messager m = Messager.getMessager();
                    m.send("StoryCard double-clicked", StoryCard.this);
                    event.setHandled(true);
                } else if (event.getButton() == 1 && event.getClickCount() == 1) {
                    // If the StoryCard is single-clicked with the left mouse
                    // button send the "StoryCard single-clicked" message.
                    Messager.getMessager().send("StoryCard single-clicked", StoryCard.this);
                    event.setHandled(true);
                }*/
            }
        });
        
        editor = new FunctionEditor(this,text);
        
        try {
            draggable = new Draggable(getNode());
            draggable.attach(this);
        } catch (NodeAlreadyDraggableException e) {
            // ...
        }       
        
        Messager.getMessager().accept("drag started", this, null);
    }
        
    public void attach(DragDropObserver o) {
        draggable.attach(o);
    }
    
    public Draggable getDraggable() {
        return draggable;
    }
                
    /**
     * Start an activity that smoothly scales the story card over time.
     * 
     * Starts a PInterpolatingActivity and stores it in this.activity. Scales
     * the story card by scaling its background node.
     * 
     * @param dest The value to scale up or down to.
     */
    private void smoothlyScale(final float dest) {
        // First make sure no other scale up or scale down activity is running.
        if (activity != null) {
            activity.terminate();
        }
        
        int duration = 150;
        int delay = 50;
        activity = new PInterpolatingActivity(
            duration,
            PUtil.DEFAULT_ACTIVITY_STEP_RATE,
            delay + System.currentTimeMillis(),
            1, // Number of times the activity should loop before ending            
            PInterpolatingActivity.SOURCE_TO_DESTINATION
        ){
            // Override some of PInterpolatingActivity's methods to make
            // something actually happen as the activity runs.
            private float source;

            /**
             * Called before the activity is scheduled to start running.
             */ 
            @Override
            protected void activityStarted() {
                source = (float)getNode().getScale();
                super.activityStarted();
            }
            /**
             * Called to set the target value at each step of the activity.
             */
            @Override
            public void setRelativeTargetValue(float scale) {
                float scaleTo = source + (scale * (dest - source));
                getNode().setScale(scaleTo);
            }
        };
        getNode().addActivity(activity);
    }
        
    public void highlight() {
        if (!highlighted && !draggable.isDragging()) {
            getNode().moveToFront();
            // FIXME: this depends on the exact structure of the scene graph.
            // Instead StoryBase should tag its StoryCards with itself.
            getNode().getParent().getParent().moveToFront();
            highlighted = true;
            smoothlyScale(1.5f);
        }
    }
    
    public void unhighlight() {
        if (highlighted && !draggable.isDragging()) {
            highlighted = false;
            smoothlyScale(1.0f);
        }
    }
                                 
    public FunctionEditor getEditor() {
        return editor;
    }
    
    @Override
    public String toString() {
        return getFunction().toString() + "\n" + editor.getText();
    }
    
    public boolean compare(Object o) {
        if (!(o instanceof StoryCard)) {
            return false;            
        } else {
            StoryCard s = (StoryCard) o;
            if (!s.getEditor().getText().equals(editor.getText())) {
                return false;
            }
            if (!s.getFunction().compare(getFunction())) {
                return false;
            }
            return true;
        }
    }
    
    public void receive(String name, Object receiver_arg, Object sender_arg) {
        if (name.equals("drag started")) {
            if (sender_arg instanceof PNode) {
                PNode node = (PNode) sender_arg;
                if (node.equals(getNode())) {
                    if (activity != null) {
                        activity.terminate();
                    }
                    getNode().setScale(1.0);
                }
            }
        }
    }
    
    public boolean notify(DropEvent de) {
        return true;
    }

    public int compareTo(Object arg) {
        StoryCard s = (StoryCard) arg;
        return s.getFunction().compareTo(this.getFunction());
    }

    // Implement Originator
    // --------------------
    
    private static final class StoryCardMemento implements Memento {
        // No need to defensively copy anything as strings are immutable and
        // FunctionMemento should be immutable.
        private final Memento functionMemento;
        private final String userText;
        StoryCardMemento (StoryCard sc) {
            this.functionMemento = sc.getFunction().createMemento();
            this.userText = sc.getEditor().getText();
        }
        Memento getFunctionMemento() { return functionMemento; }
        String getUserText() { return userText; }
    }
    
    public Memento createMemento() {
        return new StoryCardMemento(this);
    }
    
    public static StoryCard newInstanceFromMemento(Memento m) throws MementoException {
        if (m == null) {
            String detail = "Null memento object.";
            MementoException e = new MementoException(detail);
            Logger.getLogger(StoryCard.class.getName()).throwing("StoryCard", "newInstanceFromMemento", e);
            throw e;
        }
        if (!(m instanceof StoryCardMemento)) {
            String detail = "Wrong type of memento object.";
            MementoException e = new MementoException(detail);
            Logger.getLogger(StoryCard.class.getName()).throwing("StoryCard", "newInstanceFromMemento", e);
            throw e;
        }
        StoryCardMemento scm = (StoryCardMemento) m;
        Function f = Function.newInstanceFromMemento(scm.getFunctionMemento());
        return new StoryCard(f,scm.getUserText());
    }
}