/**
 * Copyright 2010 Tommi S.E. Laukkanen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vaadin.addons.lazyquerycontainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vaadin.data.Buffered;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Container.ItemSetChangeNotifier;
import com.vaadin.data.Container.PropertySetChangeNotifier;
import com.vaadin.data.Container.Sortable;
import com.vaadin.data.Validator.InvalidValueException;

/**
 * LazyQueryContainer provides lazy loading of items from business services. See package
 * level documentation for detailed description. This implements event notification functionality
 * and delegates other methods to QueryView aggregate.
 * @author Tommi S.E. Laukkanen
 */
public class LazyQueryContainer implements Indexed, Sortable, ItemSetChangeNotifier, PropertySetChangeNotifier, Buffered {
	private static final long serialVersionUID = 1L;

	private QueryView view;
	private List<ItemSetChangeListener> itemSetChangeListeners=new ArrayList<ItemSetChangeListener>();
	private List<PropertySetChangeListener> propertySetChangeListeners=new ArrayList<PropertySetChangeListener>();
	
	/**
	 * Constructs LazyQueryContainer with LazyQueryView and given queryFactory.
	 * @param queryFactory The query factory to be used.
	 * @param batchSize The batch size to be used when loading data.
	 */
	public LazyQueryContainer(QueryFactory queryFactory, int batchSize) {
		this.view=new LazyQueryView(queryFactory, batchSize);
	}

	/**
	 * Constructs LazyQueryContainer with LazyQueryView and given queryFactory and queryDefinition.
	 * @param queryFactory The query factory to be used.
	 * @param queryDefinition The query definition to be used.
	 */
	public LazyQueryContainer(QueryDefinition queryDefinition, QueryFactory queryFactory) {
	    this.view=new LazyQueryView(queryDefinition, queryFactory);
	}
	/**
	 * Constructs LazyQueryContainer with the given QueryView. This constructor 
	 * role is to enable use of custom view implementations.
	 * @param view
	 */
	public LazyQueryContainer(QueryView view) {
		this.view=view;
	}
	

	public void sort(Object[] sortPropertyIds, boolean[] ascendingStates) {
		view.sort(sortPropertyIds, ascendingStates);
	}
		

	public Collection<?> getContainerPropertyIds() {
		return view.getDefinition().getPropertyIds();
	}


	public Collection<?> getSortableContainerPropertyIds() {
		return view.getDefinition().getSortablePropertyIds();
	}
	

	public Class<?> getType(Object propertyId) {
		return view.getDefinition().getPropertyType(propertyId);
	}	
		

	public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) {
		view.getDefinition().addProperty(propertyId,type,defaultValue,true,false);
		notifyPropertySetChanged();
		return true;
	}
	
	public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue,
			boolean readOnly, boolean sortable) {
		view.getDefinition().addProperty(propertyId,type,defaultValue,readOnly,sortable);
		notifyPropertySetChanged();
		return true;
	}
	

	public boolean removeContainerProperty(Object propertyId) {
		view.getDefinition().removeProperty(propertyId);
		notifyPropertySetChanged();
		return true;
	}
			

	public int size() {
		return view.size();
	}
	

	public Collection<?> getItemIds() {
		ArrayList<Integer> ids=new ArrayList<Integer>(size());
		for(int i=0;i<size();i++) {
			ids.add(i);
		}
		return ids;
	}


	public Item getItem(Object itemId) {
		return view.getItem((Integer)itemId);
	}
	

	public Property getContainerProperty(Object itemId, Object propertyId) {
		return getItem(itemId).getItemProperty(propertyId);
	}
		

	public Object getIdByIndex(int index) {
		return index;
	}


	public int indexOfId(Object itemId) {
		return itemId.getClass()==Integer.class?(Integer)itemId:-1;
	}


	public boolean containsId(Object itemId) {
		return itemId.getClass()==Integer.class?size()>(Integer)itemId&&(Integer)itemId>=0:false;
	}
		

	public boolean isFirstId(Object itemId) {
		return itemId.getClass()==Integer.class?(Integer)itemId==0:false;
	}


	public boolean isLastId(Object itemId) {
		return itemId.getClass()==Integer.class?(Integer)itemId==size()-1:false;
	}
		

	public Object firstItemId() {
		return 0;
	}


	public Object lastItemId() {
		return size()-1;
	}


	public Object nextItemId(Object itemId) {
		return (Integer)itemId+1;
	}


	public Object prevItemId(Object itemId) {
		return (Integer)itemId-1;
	}
	

	public Object addItemAt(int index) {
		throw new UnsupportedOperationException();
	}



	public Object addItemAfter(Object previousItemId) {
		throw new UnsupportedOperationException();
	}


	public Item addItemAt(int index, Object newItemId) {
		throw new UnsupportedOperationException();
	}


	public Item addItemAfter(Object previousItemId, Object newItemId) {
		throw new UnsupportedOperationException();
	}


	public Item addItem(Object itemId) {
		throw new UnsupportedOperationException();
	}


	public Object addItem() {
		Object itemId=view.addItem();
		notifyItemSetChanged();
		return itemId;
	}


	public boolean removeItem(Object itemId) {
		view.removeItem((Integer)itemId);		
		notifyItemSetChanged();
		return true;
	}


	public boolean removeAllItems() {
		view.removeAllItems();
		refresh();
		return true;
	}


	public void addListener(ItemSetChangeListener listener) {
		itemSetChangeListeners.add(listener);
	}


	public void removeListener(ItemSetChangeListener listener) {
		itemSetChangeListeners.remove(listener);
	}
	
	public void refresh() {
		view.refresh();
		notifyItemSetChanged();
	}
	
	private void notifyItemSetChanged() {
		QueryItemSetChangeEvent event=new QueryItemSetChangeEvent(this);
		for(ItemSetChangeListener listener:itemSetChangeListeners) {
			listener.containerItemSetChange(event);
		}
	}


	public void addListener(PropertySetChangeListener listener) {
		propertySetChangeListeners.add(listener);
		
	}


	public void removeListener(PropertySetChangeListener listener) {
		propertySetChangeListeners.remove(listener);
	}
	
	private void notifyPropertySetChanged() {
		QueryPropertySetChangeEvent event=new QueryPropertySetChangeEvent(this);
		for(PropertySetChangeListener listener:propertySetChangeListeners) {
			listener.containerPropertySetChange(event);
		}
	}
	
	private class QueryItemSetChangeEvent implements ItemSetChangeEvent {

		private static final long serialVersionUID = 1L;
		
		private LazyQueryContainer container;
		
		public QueryItemSetChangeEvent(LazyQueryContainer container) {
			this.container=container;
		}
		
	
		public Container getContainer() {
			return container;
		}
		
	}
	
	private class QueryPropertySetChangeEvent implements PropertySetChangeEvent {

		private static final long serialVersionUID = 1L;
		
		private LazyQueryContainer container;
		
		public QueryPropertySetChangeEvent(LazyQueryContainer container) {
			this.container=container;
		}
		
	
		public Container getContainer() {
			return container;
		}
		
	}


	public void commit() throws SourceException, InvalidValueException {
		view.commit();
		refresh();
	}


	public void discard() throws SourceException {
		view.discard();
		refresh();
	}


	public boolean isModified() {
		return view.isModified();
	}

	public boolean isReadThrough() {
		return false;
	}


	public boolean isWriteThrough() {
		return false;
	}


	public void setReadThrough(boolean readThrough) throws SourceException {
		throw new UnsupportedOperationException();
	}


	public void setWriteThrough(boolean writeThrough) throws SourceException,
			InvalidValueException {
		throw new UnsupportedOperationException();
	}
}