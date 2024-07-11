package com.example.appbansach;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbansach.Adapter.CartItemAdapter;
import com.example.appbansach.modle.CartItem;
import com.example.appbansach.modle.Invoice;
import com.example.appbansach.modle.InvoiceGenerator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InvoiceActivity extends AppCompatActivity {
    private TextView customerNameView;
    private TextView timeIssuedView;
    private TextView totalAmountView;
    private RecyclerView cartItemsView;
    private CartItemAdapter cartItemAdapter;
    private DatabaseReference databaseReference;

    private List<CartItem> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        customerNameView = findViewById(R.id.customerNameView);
        timeIssuedView = findViewById(R.id.timeIssuedView);
        totalAmountView = findViewById(R.id.totalAmountView);
        cartItemsView = findViewById(R.id.cartItemsView);

        databaseReference = FirebaseDatabase.getInstance().getReference("carts");

        cartItems = new ArrayList<>();

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        if (username != null) {
            fetchCartItemsFromFirebase(username);
        } else {
            Toast.makeText(this, "Không cung cấp tên khách hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchCartItemsFromFirebase(String customerName) {
        databaseReference.orderByChild("tenKH").equalTo(customerName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cartItems.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        CartItem cartItem = snapshot.getValue(CartItem.class);
                        String tenKH = snapshot.child("tenKH").getValue(String.class);
                        if (cartItem != null && customerName.equals(tenKH)) {
                            cartItems.add(cartItem);
                        }
                    } catch (Exception e) {
                        Log.e("InvoiceActivity", "Lỗi khi lấy thông tin giỏ hàng: " + e.getMessage());
                    }
                }

                if (cartItems.isEmpty()) {
                    Toast.makeText(InvoiceActivity.this, "Không tìm thấy sản phẩm cho khách hàng " + customerName, Toast.LENGTH_SHORT).show();
                } else {
                    // Hiển thị thông tin giỏ hàng
                    cartItemAdapter = new CartItemAdapter(cartItems);
                    cartItemsView.setLayoutManager(new LinearLayoutManager(InvoiceActivity.this));
                    cartItemsView.setAdapter(cartItemAdapter);
                    // Tạo và hiển thị hóa đơn ngay lập tức
                    createInvoiceAndDisplay(customerName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(InvoiceActivity.this, "Đã xảy ra lỗi, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createInvoiceAndDisplay(String customerName) {
        InvoiceGenerator invoiceGenerator = new InvoiceGenerator();
        Invoice invoice = invoiceGenerator.createInvoice(customerName, cartItems);

        customerNameView.setText(invoice.getCustomerName());
        timeIssuedView.setText(invoice.getTimeIssued());
        totalAmountView.setText(String.format("%.2f", invoice.getTotalAmount()));

        cartItemAdapter = new CartItemAdapter(invoice.getCartItems());
        cartItemsView.setLayoutManager(new LinearLayoutManager(this));
        cartItemsView.setAdapter(cartItemAdapter);

        saveInvoiceToFirebase(invoice, customerName);
        deleteCartItems(customerName);

        Intent intent = new Intent(InvoiceActivity.this, ListBookActivity.class);
        intent.putExtra("username", customerName);
        startActivity(intent);
    }

    private void saveInvoiceToFirebase(Invoice invoice, String name) {
        DatabaseReference invoicesRef = FirebaseDatabase.getInstance().getReference("invoices");

        String invoiceId = invoicesRef.push().getKey();
        if (invoiceId != null) {
            invoicesRef.child(invoiceId).setValue(invoice).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(InvoiceActivity.this, "Hóa đơn đã được lưu thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(InvoiceActivity.this, "Lưu hóa đơn thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteCartItems(String customerName) {
        databaseReference.orderByChild("tenKH").equalTo(customerName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("InvoiceActivity", "Item deleted successfully");
                        } else {
                            Log.e("InvoiceActivity", "Failed to delete item: " + task.getException().getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(InvoiceActivity.this, "Error occurred while deleting items", Toast.LENGTH_SHORT).show();
            }
        });
    }
}