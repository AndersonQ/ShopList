package br.eti.andersonq;

/**
 * Class to store items informations
 * @author ainsoph
 *
 */
public class Item {

	public	long id;
	public	long listId;
	public	String name;
	public	int quantity;
	public	float price;
	public	int purchased;
	
	/**
	 * 
	 * @param id
	 * @param listId
	 * @param name
	 * @param quantity
	 * @param price
	 * @param purchased
	 */
	public Item(long id, long listId, String name, int quantity, float price, int purchased)
	{
		this.id = id;
		this.listId = listId;
		this.name = name;
		this.quantity = quantity;
		this.price = price;
		this.purchased = purchased;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getListId() {
		return listId;
	}

	public void setListId(long listId) {
		this.listId = listId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public int getPurchased() {
		return purchased;
	}
	
	public boolean getPurchasedBool() {
		return purchased != 0 ? true : false;
	}

	public void setPurchased(int purchased) {
		this.purchased = purchased;
	}
}
