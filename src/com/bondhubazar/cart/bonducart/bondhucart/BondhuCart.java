
package com.bondhubazar.cart.bonducart;

// Map এবং HashMap ব্যবহার করার জন্য
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;

// App Inventor Extension
import com.google.appinventor.components.annotations.Extension;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleEvent;

import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

// App Inventor List
import com.google.appinventor.components.runtime.util.YailList;


@Extension(
    description = "Bondhu Bazar Shopping Cart Extension",
)

public class BondhuCart
        extends AndroidNonvisibleComponent {

    // =====================================
    // ১. Cart-এর একটি Product-এর তথ্য
    // =====================================

    private static class CartItem {

        // Product ID
        String id;

        // Product Name
        String name;

        // Product Price
        double price;

        // Product Quantity
        int quantity;

        // Product Stock
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

        // Product-এর মোট দাম
        double getTotal() {

            return price * quantity;
        }
    }

    // =====================================
    // ২. Cart-এর HashMap
    // =====================================

    // LinkedHashMap ব্যবহার করছি যাতে
    // Product যোগ করার ক্রম বজায় থাকে
    private Map<String, CartItem> cart =
            new LinkedHashMap<>();

    // =====================================
    // ৩. Constructor
    // =====================================

    public BondhuCart(ComponentContainer container) {

        super(container.$form());
    }

    // =====================================
    // ৪. Add To Cart
    // =====================================

    @SimpleFunction(
        description = "Cart-এ একটি Product যোগ করুন"
    )
    public void AddToCart(
            String productId,
            String productName,
            double price,
            int stock
    ) {

        try {

            // ভুল তথ্য পরীক্ষা
            if (productId == null || productId.trim().isEmpty()) {

                ErrorOccurred("Product ID খালি রাখা যাবে না");
                return;
            }

            if (price < 0 || stock < 0) {

                ErrorOccurred("Price অথবা Stock ভুল");
                return;
            }

            // Product আগে Cart-এ আছে কি না
            CartItem item = cart.get(productId);

            if (item == null) {

                // Stock না থাকলে যোগ হবে না
                if (stock == 0) {

                    StockLimitReached(productId);
                    return;
                }

                // নতুন Product তৈরি
                item = new CartItem(
                        productId,
                        productName,
                        price,
                        1,
                        stock
                );

                // Cart-এ যোগ
                cart.put(productId, item);

            } else {

                // Product-এর নতুন তথ্য আপডেট
                item.name = productName;
                item.price = price;
                item.stock = stock;

                // Stock পরীক্ষা
                if (item.quantity >= item.stock) {

                    StockLimitReached(productId);
                    return;
                }

                // Quantity বাড়ানো
                item.quantity++;
            }

            // Cart Update Event
            CartUpdated(
                    productId,
                    item.quantity,
                    GetCartTotal()
            );

        } catch (Exception e) {

            ErrorOccurred("AddToCart: " + e.getMessage());
        }
    }

    // =====================================
    // ৫. Quantity বাড়ানো
    // =====================================

    @SimpleFunction(
        description = "Product Quantity এক বাড়ান"
    )
    public void IncreaseQuantity(String productId) {

        CartItem item = cart.get(productId);

        if (item == null) {

            ErrorOccurred("Product Cart-এ নেই");
            return;
        }

        // Stock Limit পরীক্ষা
        if (item.quantity >= item.stock) {

            StockLimitReached(productId);
            return;
        }

        // Quantity বাড়ানো
        item.quantity++;

        // Update Event
        CartUpdated(
                productId,
                item.quantity,
                GetCartTotal()
        );
    }

    // =====================================
    // ৬. Quantity কমানো
    // =====================================

    @SimpleFunction(
        description = "Product Quantity এক কমান"
    )
    public void DecreaseQuantity(String productId) {

        CartItem item = cart.get(productId);

        if (item == null) {

            ErrorOccurred("Product Cart-এ নেই");
            return;
        }

        // Quantity এক কমানো
        item.quantity--;

        // Quantity শূন্য হলে Product Delete
        if (item.quantity <= 0) {

            cart.remove(productId);

            ItemRemoved(productId);

        } else {

            CartUpdated(
                    productId,
                    item.quantity,
                    GetCartTotal()
            );
        }
    }

    // =====================================
    // ৭. নির্দিষ্ট Product Remove
    // =====================================

    @SimpleFunction(
        description = "Cart থেকে Product Delete করুন"
    )
    public void RemoveFromCart(String productId) {

        // Product আছে কি না পরীক্ষা
        if (cart.containsKey(productId)) {

            // Product Delete
            cart.remove(productId);

            // Delete Event
            ItemRemoved(productId);

        } else {

            ErrorOccurred("Product Cart-এ পাওয়া যায়নি");
        }
    }

    // =====================================
    // ৮. Quantity পাওয়া
    // =====================================

    @SimpleFunction(
        description = "Product-এর বর্তমান Quantity জানুন"
    )
    public int GetQuantity(String productId) {

        CartItem item = cart.get(productId);

        if (item != null) {

            return item.quantity;
        }

        return 0;
    }

    // =====================================
    // ৯. Product Price পাওয়া
    // =====================================

    @SimpleFunction(
        description = "Product-এর Unit Price জানুন"
    )
    public double GetItemPrice(String productId) {

        CartItem item = cart.get(productId);

        if (item != null) {

            return item.price;
        }

        return 0;
    }

    // =====================================
    // ১০. Product Total পাওয়া
    // =====================================

    @SimpleFunction(
        description = "একটি Product-এর মোট দাম জানুন"
    )
    public double GetItemTotal(String productId) {

        CartItem item = cart.get(productId);

        if (item != null) {

            return item.getTotal();
        }

        return 0;
    }

    // =====================================
    // ১১. সম্পূর্ণ Cart-এর Total
    // =====================================

    @SimpleFunction(
        description = "সম্পূর্ণ Cart-এর মোট দাম হিসাব করুন"
    )
    public double GetCartTotal() {

        double total = 0;

        // প্রতিটি Product-এর দাম যোগ
        for (CartItem item : cart.values()) {

            total += item.getTotal();
        }

        return total;
    }

    // =====================================
    // ১২. Cart-এ মোট Product Quantity
    // =====================================

    @SimpleFunction(
        description = "Cart-এর সব Product-এর Quantity যোগ করুন"
    )
    public int GetCartCount() {

        int count = 0;

        for (CartItem item : cart.values()) {

            count += item.quantity;
        }

        return count;
    }

    // =====================================
    // ১৩. Cart-এ কত ধরনের Product আছে
    // =====================================

    @SimpleFunction(
        description = "Cart-এ কত ধরনের Product আছে জানুন"
    )
    public int GetUniqueItemCount() {

        return cart.size();
    }

    // =====================================
    // ১৪. Product আছে কি না পরীক্ষা
    // =====================================

    @SimpleFunction(
        description = "Product Cart-এ আছে কি না পরীক্ষা করুন"
    )
    public boolean ContainsProduct(String productId) {

        return cart.containsKey(productId);
    }

    // =====================================
    // ১৫. Stock পরিবর্তন
    // =====================================

    @SimpleFunction(
        description = "Cart-এর Product-এর Stock Limit পরিবর্তন করুন"
    )
    public void SetStock(String productId, int stock) {

        CartItem item = cart.get(productId);

        if (item == null) {

            ErrorOccurred("Product Cart-এ নেই");
            return;
        }

        if (stock < 0) {

            ErrorOccurred("Stock ঋণাত্মক হতে পারে না");
            return;
        }

        item.stock = stock;

        // নতুন Stock Quantity-এর নিচে হলে
        if (item.quantity > stock) {

            item.quantity = stock;

            if (item.quantity == 0) {

                cart.remove(productId);
                ItemRemoved(productId);

                return;
            }
        }

        CartUpdated(
                productId,
                item.quantity,
                GetCartTotal()
        );
    }

    // =====================================
    // ১৬. Product Price Update
    // =====================================

    @SimpleFunction(
        description = "Cart-এর Product-এর দাম পরিবর্তন করুন"
    )
    public void UpdateItemPrice(
            String productId,
            double newPrice
    ) {

        CartItem item = cart.get(productId);

        if (item == null) {

            ErrorOccurred("Product Cart-এ নেই");
            return;
        }

        if (newPrice < 0) {

            ErrorOccurred("Price ভুল");
            return;
        }

        item.price = newPrice;

        CartUpdated(
                productId,
                item.quantity,
                GetCartTotal()
        );
    }

    // =====================================
    // ১৭. Cart-এর সমস্ত Data পাওয়া
    // =====================================

    @SimpleFunction(
        description = "Cart-এর সমস্ত Product Data List আকারে নিন"
    )
    public YailList GetCartData() {

        // Result List
        List<Object> result = new ArrayList<>();

        // প্রতিটি Product-এর তথ্য
        for (CartItem item : cart.values()) {

            // একটি Product-এর তথ্যের List
            List<Object> row = new ArrayList<>();

            row.add(item.id);
            row.add(item.name);
            row.add(item.price);
            row.add(item.quantity);
            row.add(item.stock);
            row.add(item.getTotal());

            // Main List-এ যোগ
            result.add(
                    YailList.makeList(row)
            );
        }

        // App Inventor List ফেরত
        return YailList.makeList(result);
    }

    // =====================================
    // ১৮. Cart Clear করা
    // =====================================

    @SimpleFunction(
        description = "Cart-এর সমস্ত Product Delete করুন"
    )
    public void ClearCart() {

        // সমস্ত Product মুছে ফেলা
        cart.clear();

        // Cart Empty Event
        CartCleared();
    }

    // =====================================
    // ১৯. Cart Updated Event
    // =====================================

    @SimpleEvent(
        description = "Cart Update হলে এই Event চালু হবে"
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

    // =====================================
    // ২০. Product Removed Event
    // =====================================

    @SimpleEvent(
        description = "Product Delete হলে এই Event চালু হবে"
    )
    public void ItemRemoved(String productId) {

        EventDispatcher.dispatchEvent(
                this,
                "ItemRemoved",
                productId
        );
    }

    // =====================================
    // ২১. Stock Limit Event
    // =====================================

    @SimpleEvent(
        description = "Stock Limit শেষ হলে এই Event চালু হবে"
    )
    public void StockLimitReached(String productId) {

        EventDispatcher.dispatchEvent(
                this,
                "StockLimitReached",
                productId
        );
    }

    // =====================================
    // ২২. Cart Cleared Event
    // =====================================

    @SimpleEvent(
        description = "Cart সম্পূর্ণ খালি হলে Event চালু হবে"
    )
    public void CartCleared() {

        EventDispatcher.dispatchEvent(
                this,
                "CartCleared"
        );
    }

    // =====================================
    // ২৩. Error Event
    // =====================================

    @SimpleEvent(
        description = "কোনো সমস্যা হলে Error Message পাঠাবে"
    )
    public void ErrorOccurred(String message) {

        EventDispatcher.dispatchEvent(
                this,
                "ErrorOccurred",
                message
        );
    }
}
package com.bondhubazar.cart.bonducart.bondhucart;

import com.google.appinventor.components.annotations.Extension;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;

@Extension(
        description = "Extension component for BondhuCart. Built with <3 and Rush.",
        icon = "icon.png"
)
public class BondhuCart extends AndroidNonvisibleComponent {

    public BondhuCart(ComponentContainer container) {
        super(container.$form());
    }

    @SimpleFunction(description = "Returns the sum of the given list of integers.")
    public int SumAll(YailList listOfInts) {
        int sum = 0;

        for (final Object o : listOfInts.toArray()) {
            try {
                sum += Integer.parseInt(o.toString());
            } catch (NumberFormatException e) {
                throw new YailRuntimeError(e.toString(), "NumberFormatException");
            }
        }

        return sum;
    }
}
