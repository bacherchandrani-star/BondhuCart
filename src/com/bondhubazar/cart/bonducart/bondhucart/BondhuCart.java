
package com.bondhubazar.cart.bonducart;

// Java Collections
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;

// App Inventor Annotations
import com.google.appinventor.components.annotations.Extension;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleEvent;

// App Inventor Runtime
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

// App Inventor List
import com.google.appinventor.components.runtime.util.YailList;


@Extension(
    description = "Bondhu Bazar Shopping Cart Extension V2"
)

public class BondhuCart extends AndroidNonvisibleComponent {

    // =========================================
    // ১. Cart Item Class
    // =========================================

    private static class CartItem {

        String id;
        String name;
        double price;
        int quantity;
        int stock;

        // Constructor
        CartItem(
            String id,
            String name,
            double price,
            int quantity,
            int stock
        ) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.stock = stock;
        }

        // Product Total
        double getTotal() {
            return price * quantity;
        }
    }

    // =========================================
    // ২. Cart HashMap
    // =========================================

    // LinkedHashMap Product-এর যোগ করার ক্রম রাখে
    private final Map<String, CartItem> cart =
        new LinkedHashMap<>();

    // =========================================
    // ৩. Constructor
    // =========================================

    public BondhuCart(ComponentContainer container) {
        super(container.$form());
    }

    // =========================================
    // ৪. Common Validation
    // =========================================

    // Product ID পরীক্ষা
    private String validId(String productId) {

        if (productId == null ||
            productId.trim().isEmpty()) {

            ErrorOccurred("Product ID খালি রাখা যাবে না");
            return null;
        }

        return productId.trim();
    }

    // Price পরীক্ষা
    private boolean validPrice(double price) {

        if (Double.isNaN(price) ||
            Double.isInfinite(price) ||
            price < 0) {

            ErrorOccurred("Product Price সঠিক নয়");
            return false;
        }

        return true;
    }

    // =========================================
    // ৫. Common Cart Update
    // =========================================

    private void notifyCartChanged(String productId) {

        CartItem item = cart.get(productId);

        int quantity = 0;

        if (item != null) {
            quantity = item.quantity;
        }

        // Product Update Event
        CartUpdated(
            productId,
            quantity,
            GetCartTotal()
        );

        // Total Update Event
        CartTotalChanged(GetCartTotal());
    }

    // =========================================
    // ৬. Add To Cart
    // =========================================

    @SimpleFunction(
        description = "Cart-এ Product যোগ করুন"
    )
    public void AddToCart(
        String productId,
        String productName,
        double price,
        int stock
    ) {

        String id = validId(productId);

        if (id == null) return;

        if (!validPrice(price)) return;

        if (stock < 0) {
            ErrorOccurred("Stock ঋণাত্মক হতে পারে না");
            return;
        }

        if (productName == null ||
            productName.trim().isEmpty()) {

            ErrorOccurred("Product Name খালি");
            return;
        }

        CartItem item = cart.get(id);

        // Stock শূন্য হলে Product যোগ হবে না
        if (stock == 0) {

            if (item != null) {
                cart.remove(id);
                ItemRemoved(id);
                CartTotalChanged(GetCartTotal());
            } else {
                StockLimitReached(id);
            }

            return;
        }

        // নতুন Product
        if (item == null) {

            item = new CartItem(
                id,
                productName.trim(),
                price,
                1,
                stock
            );

            cart.put(id, item);

        } else {

            // Product তথ্য আপডেট
            item.name = productName.trim();
            item.price = price;
            item.stock = stock;

            // Stock অনুযায়ী Quantity ঠিক করা
            if (item.quantity > stock) {
                item.quantity = stock;
                notifyCartChanged(id);
                return;
            }

            // Stock Limit পরীক্ষা
            if (item.quantity >= stock) {
                StockLimitReached(id);
                notifyCartChanged(id);
                return;
            }

            // Quantity বৃদ্ধি
            item.quantity++;
        }

        notifyCartChanged(id);
    }

    // =========================================
    // ৭. Increase Quantity
    // =========================================

    @SimpleFunction(
        description = "Product Quantity এক বাড়ান"
    )
    public void IncreaseQuantity(String productId) {

        String id = validId(productId);
        if (id == null) return;

        CartItem item = cart.get(id);

        if (item == null) {
            ErrorOccurred("Product Cart-এ নেই");
            return;
        }

        if (item.quantity >= item.stock) {
            StockLimitReached(id);
            return;
        }

        item.quantity++;

        notifyCartChanged(id);
    }

    // =========================================
    // ৮. Decrease Quantity
    // =========================================

    @SimpleFunction(
        description = "Product Quantity এক কমান"
    )
    public void DecreaseQuantity(String productId) {

        String id = validId(productId);
        if (id == null) return;

        CartItem item = cart.get(id);

        if (item == null) {
            ErrorOccurred("Product Cart-এ নেই");
            return;
        }

        item.quantity--;

        if (item.quantity <= 0) {

            cart.remove(id);

            ItemRemoved(id);

            CartTotalChanged(GetCartTotal());

        } else {

            notifyCartChanged(id);
        }
    }

    // =========================================
    // ৯. Remove From Cart
    // =========================================

    @SimpleFunction(
        description = "Cart থেকে Product Delete করুন"
    )
    public void RemoveFromCart(String productId) {

        String id = validId(productId);
        if (id == null) return;

        if (cart.remove(id) != null) {

            ItemRemoved(id);

            CartTotalChanged(GetCartTotal());

        } else {

            ErrorOccurred("Product Cart-এ নেই");
        }
    }

    // =========================================
    // ১০. Get Quantity
    // =========================================

    @SimpleFunction(
        description = "Product Quantity জানুন"
    )
    public int GetQuantity(String productId) {

        String id = validId(productId);
        if (id == null) return 0;

        CartItem item = cart.get(id);

        return item == null ? 0 : item.quantity;
    }

    // =========================================
    // ১১. Get Item Price
    // =========================================

    @SimpleFunction(
        description = "Product Unit Price জানুন"
    )
    public double GetItemPrice(String productId) {

        String id = validId(productId);
        if (id == null) return 0;

        CartItem item = cart.get(id);

        return item == null ? 0 : item.price;
    }

    // =========================================
    // ১২. Get Item Total
    // =========================================

    @SimpleFunction(
        description = "Product-এর মোট দাম জানুন"
    )
    public double GetItemTotal(String productId) {

        String id = validId(productId);
        if (id == null) return 0;

        CartItem item = cart.get(id);

        return item == null ? 0 : item.getTotal();
    }

    // =========================================
    // ১৩. Get Cart Total
    // =========================================

    @SimpleFunction(
        description = "সম্পূর্ণ Cart-এর Total জানুন"
    )
    public double GetCartTotal() {

        double total = 0;

        for (CartItem item : cart.values()) {
            total += item.getTotal();
        }

        return total;
    }

    // =========================================
    // ১৪. Get Cart Count
    // =========================================

    @SimpleFunction(
        description = "Cart-এর মোট Quantity জানুন"
    )
    public int GetCartCount() {

        int count = 0;

        for (CartItem item : cart.values()) {
            count += item.quantity;
        }

        return count;
    }

    // =========================================
    // ১৫. Get Unique Item Count
    // =========================================

    @SimpleFunction(
        description = "Cart-এ কত ধরনের Product আছে"
    )
    public int GetUniqueItemCount() {

        return cart.size();
    }

    // =========================================
    // ১৬. Contains Product
    // =========================================

    @SimpleFunction(
        description = "Product Cart-এ আছে কি না"
    )
    public boolean ContainsProduct(String productId) {

        String id = validId(productId);
        if (id == null) return false;

        return cart.containsKey(id);
    }

    // =========================================
    // ১৭. Set Stock
    // =========================================

    @SimpleFunction(
        description = "Product-এর Stock পরিবর্তন করুন"
    )
    public void SetStock(
        String productId,
        int stock
    ) {

        String id = validId(productId);
        if (id == null) return;

        if (stock < 0) {
            ErrorOccurred("Stock ঋণাত্মক হতে পারে না");
            return;
        }

        CartItem item = cart.get(id);

        if (item == null) {
            ErrorOccurred("Product Cart-এ নেই");
            return;
        }

        item.stock = stock;

        // Stock শূন্য হলে Product Remove
        if (stock == 0) {

            cart.remove(id);

            ItemRemoved(id);

            CartTotalChanged(GetCartTotal());

            return;
        }

        // Stock Quantity-এর নিচে হলে Quantity কমবে
        if (item.quantity > stock) {
            item.quantity = stock;
        }

        notifyCartChanged(id);
    }

    // =========================================
    // ১৮. Update Item Price
    // =========================================

    @SimpleFunction(
        description = "Product Price পরিবর্তন করুন"
    )
    public void UpdateItemPrice(
        String productId,
        double newPrice
    ) {

        String id = validId(productId);
        if (id == null) return;

        if (!validPrice(newPrice)) return;

        CartItem item = cart.get(id);

        if (item == null) {
            ErrorOccurred("Product Cart-এ নেই");
            return;
        }

        item.price = newPrice;

        notifyCartChanged(id);
    }

    // =========================================
    // ১৯. Get Cart Data
    // =========================================

    @SimpleFunction(
        description = "সম্পূর্ণ Cart Data List আকারে নিন"
    )
    public YailList GetCartData() {

        List<Object> result = new ArrayList<>();

        for (CartItem item : cart.values()) {

            List<Object> row = new ArrayList<>();

            // Column 1: Product ID
            row.add(item.id);

            // Column 2: Product Name
            row.add(item.name);

            // Column 3: Unit Price
            row.add(item.price);

            // Column 4: Quantity
            row.add(item.quantity);

            // Column 5: Stock
            row.add(item.stock);

            // Column 6: Total Price
            row.add(item.getTotal());

            result.add(YailList.makeList(row));
        }

        return YailList.makeList(result);
    }

    // =========================================
    // ২০. Clear Cart
    // =========================================

    @SimpleFunction(
        description = "সম্পূর্ণ Cart খালি করুন"
    )
    public void ClearCart() {

        cart.clear();

        CartCleared();

        CartTotalChanged(0);
    }

    // =========================================
    // ২১. Cart Updated Event
    // =========================================

    @SimpleEvent(
        description = "Cart Product Update হলে"
    )
    public void CartUpdated(
        String productId,
        int quantity,
        double cartTotal
    ) {

        EventDispatcher.dispatchEvent(
            this,
            "CartUpdated",
            productId,
            quantity,
            cartTotal
        );
    }

    // =========================================
    // ২২. Item Removed Event
    // =========================================

    @SimpleEvent(
        description = "Product Remove হলে"
    )
    public void ItemRemoved(String productId) {

        EventDispatcher.dispatchEvent(
            this,
            "ItemRemoved",
            productId
        );
    }

    // =========================================
    // ২৩. Stock Limit Event
    // =========================================

    @SimpleEvent(
        description = "Stock Limit শেষ হলে"
    )
    public void StockLimitReached(String productId) {

        EventDispatcher.dispatchEvent(
            this,
            "StockLimitReached",
            productId
        );
    }

    // =========================================
    // ২৪. Cart Cleared Event
    // =========================================

    @SimpleEvent(
        description = "সম্পূর্ণ Cart খালি হলে"
    )
    public void CartCleared() {

        EventDispatcher.dispatchEvent(
            this,
            "CartCleared"
        );
    }

    // =========================================
    // ২৫. Cart Total Changed Event
    // =========================================

    @SimpleEvent(
        description = "Cart Total পরিবর্তন হলে"
    )
    public void CartTotalChanged(double total) {

        EventDispatcher.dispatchEvent(
            this,
            "CartTotalChanged",
            total
        );
    }

    // =========================================
    // ২৬. Error Event
    // =========================================

    @SimpleEvent(
        description = "কোনো Error হলে"
    )
    public void ErrorOccurred(String message) {

        EventDispatcher.dispatchEvent(
            this,
            "ErrorOccurred",
            message
        );
    }
}
