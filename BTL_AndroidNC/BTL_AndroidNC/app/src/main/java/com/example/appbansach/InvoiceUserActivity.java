package com.example.appbansach;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appbansach.Adapter.InvoiceAdapter;
import com.example.appbansach.modle.Invoice;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InvoiceUserActivity extends AppCompatActivity {
    private ListView invoicesListView;
    private InvoiceAdapter invoiceAdapter;
    private DatabaseReference databaseReference;
    private List<Invoice> invoices;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_user); // Ensure this layout file contains ListView with the specified ID

        // Initialize the ListView
        invoicesListView = findViewById(R.id.invoicesListView);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        String role = intent.getStringExtra("role");

        // Check if invoicesListView is null
        if (invoicesListView == null) {
            Log.e("InvoiceUserActivity", "ListView with ID R.id.invoicesListView not found");
            return; // Exit early to avoid further NullPointerException
        }

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("invoices");
        invoices = new ArrayList<>();

        // Initialize the adapter and set it to the ListView
        invoiceAdapter = new InvoiceAdapter(this, invoices);
        invoicesListView.setAdapter(invoiceAdapter);

        // Fetch invoices based on user role
        if ("admin".equals(role)) {
            fetchAllInvoicesAdmin();
        } else if ("user".equals(role) && username != null) {
            fetchAllInvoices(username);
        } else {
            Toast.makeText(this, "Role or username is missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAllInvoices(String username) {
        // Query to filter invoices based on customerName
        Query query = databaseReference.orderByChild("customerName").equalTo(username);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                invoices.clear(); // Clear the old data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Invoice invoice = snapshot.getValue(Invoice.class);
                    if (invoice != null) {
                        invoices.add(invoice);
                    }
                }
                invoiceAdapter.notifyDataSetChanged(); // Notify the adapter that data has changed
                if (invoices.isEmpty()) {
                    Toast.makeText(InvoiceUserActivity.this, "Danh sách hóa đơn rỗng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(InvoiceUserActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAllInvoicesAdmin() {
        // Get all invoices from the database reference without any filtering
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                invoices.clear(); // Clear the old data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Invoice invoice = snapshot.getValue(Invoice.class);
                    if (invoice != null) {
                        invoices.add(invoice);
                    }
                }
                invoiceAdapter.notifyDataSetChanged(); // Notify the adapter that data has changed
                if (invoices.isEmpty()) {
                    Toast.makeText(InvoiceUserActivity.this, "Danh sách hóa đơn rỗng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(InvoiceUserActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
