package com.example.appbansach;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appbansach.Adapter.CartAdapter;
import com.example.appbansach.modle.CartItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CartsActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private ArrayList<CartItem> cartItems;
    private CartAdapter cartAdapter;
    private ListView cartListView;
    private String name;
    private Toolbar mlToolbarCart;
    private Button btnThanhToan;
    private String accountType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carts);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        accountType = intent.getStringExtra("role");
        Toast.makeText(CartsActivity.this, "" + username, Toast.LENGTH_SHORT).show();

        TextView totalPriceTextView = findViewById(R.id.tvTotal);
        mlToolbarCart = findViewById(R.id.toolbarCart);
        btnThanhToan = findViewById(R.id.btnThanhToan);
        setSupportActionBar(mlToolbarCart);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Giỏ Hàng");

        name = username;
        databaseReference = FirebaseDatabase.getInstance().getReference().child("carts");

        cartListView = findViewById(R.id.listViewCarts);
        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartItems, totalPriceTextView, false);
        cartListView.setAdapter(cartAdapter);

        getCartsFromDatabase();

        btnThanhToan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CartsActivity.this, AddOrderActivity.class);
                intent.putExtra("username", name);
                intent.putExtra("role",accountType);
                startActivity(intent);
            }
        });
    }

    private void getCartsFromDatabase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cartItems.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        CartItem cartItem = snapshot.getValue(CartItem.class);
                        String tenKH = snapshot.child("tenKH").getValue(String.class);
                        if (cartItem != null && name.equals(tenKH)) {
                            cartItems.add(cartItem);
                        }
                    } catch (Exception e) {
                        Log.e("CartsActivity", "Lỗi khi lấy thông tin giỏ hàng: " + e.getMessage());
                    }
                }
                cartAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CartsActivity.this, "Đã xảy ra lỗi, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
