package de.hackermuehle.pdfpresenter.model.annotations;

import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A list of actions. Actions can be performed on an ActionList through
 * {@link #insert(Annotation)}, {@link #remove(Annotation)} and {@link #reset()}.
 */
public class ActionList {
	private LinkedList<Action> _activeActions = new LinkedList<Action>();
	private LinkedList<Action> _undoneActions = new LinkedList<Action>();
	private Action _currentAction = new Action();
	private LinkedList<Annotation> _active = new LinkedList<Annotation>();
	
	private static class Step {
		static enum Type {
			INSERT,
			REMOVE
		}
		
		public Annotation _annotation;
		public Type _type;
		public int _index;
	
		public Step(Annotation annotation, Type type, int index) {
			_annotation = annotation;
			_type = type;
			_index = index;
		}
	}
	
	/**
	 * An action describes insertions and removals of annotations that can be
	 * undone/redone in one singular step.
	 */
	private class Action {
		private LinkedList<Step> _steps = new LinkedList<Step>();
		private Rectangle2D _bounds = null;
		
		public void insert(Annotation annotation) {
			_steps.push(new Step(annotation, Step.Type.INSERT, _active.size()));
			_active.add(annotation);
			
			// Update bounds:
			if (_bounds == null) _bounds = annotation.getBounds();
			else _bounds.add(annotation.getBounds());
		}
		
		public void remove(Annotation annotation) {
			int index = _active.indexOf(annotation);
			if (index == -1) throw new NoSuchElementException("annotation not on list");
			
			_steps.push(new Step(annotation, Step.Type.REMOVE, index));
			_active.remove(index);
			
			// Update bounds:
			if (_bounds == null) _bounds = annotation.getBounds();
			else _bounds.add(annotation.getBounds());
		}
		
		public void reset() {
			while (!_active.isEmpty()) {
				remove(_active.getFirst());
			}
		}
		
		public void undo() {
			try {
				for (Step step : _steps) {
					if (step._type == Step.Type.INSERT) {
						_active.remove(step._annotation);
					}
					else if (step._type == Step.Type.REMOVE) {
						_active.add(step._index, step._annotation);
					}
				}
			} catch (IndexOutOfBoundsException e) {
				throw new IllegalStateException();
			}
		}
		
		public void redo() {
			try {
				for (Step step : _steps) {
					if (step._type == Step.Type.INSERT) {
						_active.add(step._index, step._annotation);
					}
					else if (step._type == Step.Type.REMOVE) {
						_active.remove(step._annotation);
					}
				}
			} catch (IndexOutOfBoundsException e) {
				throw new IllegalStateException();
			}
		}
		
		/**
		 * @return The combined bounds of all annotations inserted and removed.
		 */
		public Rectangle2D getBounds() {
			if (_bounds == null) return new Rectangle2D.Double(0, 0, 0, 0);
			else return (Rectangle2D) _bounds.clone();
		}

		/**
		 * @return True if no annotations where added or removed since construction
		 * or the last call to {@link #undo()}.
		 */
		public boolean isEmpty() {
			return _steps.isEmpty();
		}
	};
	
	/**
	 * This action will "insert" the given annotation. 
	 * 
	 * @param annotation Annotation to insert
	 */
	public void insert(Annotation annotation) {
		_currentAction.insert(annotation);
		_undoneActions.clear();
	}
	
	/**
	 * This action will "remove" the given annotation.
	 * 
	 * @param annotation Annotation to remove
	 * @throws NoSuchElementException
	 */
	public void remove(Annotation annotation) {
		try {
			_currentAction.remove(annotation);
			_undoneActions.clear();
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException("annotation not in list");
		}
	}
	
	public void reset() {
		_currentAction.reset();
		_undoneActions.clear();
	}
	
	public boolean conclude() {
		if (_currentAction.isEmpty()) {
			return false;
		}
		_activeActions.push(_currentAction);
		_currentAction = new Action();
		return true;
	}
	
	public boolean canUndo() {
		return !_activeActions.isEmpty() || !_currentAction.isEmpty();
	}
	
	public boolean canRedo() {
		return !_undoneActions.isEmpty();
	}
	
	/**
	 * This action undoes any insertion / removal via {@link #insert(Annotation)}
	 * and {@link #remove(Annotation)}.
	 */
	public Rectangle2D undo() {
		try {
			Action action;
			if (_currentAction.isEmpty()) _currentAction = _activeActions.pop();
			action = _currentAction;
			action.undo();
			_currentAction = new Action();
			_undoneActions.push(action);
			return action.getBounds();
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException("no action to undo");
		}
	}
	
	/**
	 * This action redoes any insertion and removal via {@link #insert(Annotation)}
	 * and {@link #remove(Annotation)} previously undone via {@link #undo()}.
	 */
	public Rectangle2D redo() {
		try {
			Action action = _undoneActions.pop();
			action.redo();
			_activeActions.push(action);
			return action.getBounds();
		} catch (NoSuchElementException e) {
			throw new NoSuchElementException("no action to redo");
		}
	}

	public List<Annotation> getAnnotations() {
		return (List<Annotation>) Collections.unmodifiableList(_active);
	}
}
