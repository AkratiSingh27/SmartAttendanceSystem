package com.maekotech.smartattendancesystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class FormActivity extends AppCompatActivity {

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText notesEditText;
    private Button buttonSubmit;

    // Define the email regex pattern
    private static final String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    interface ContactApi {
        @POST("/contact-form/")
        Call<String> submitContactForm(@Body ContactRequest contactRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        notesEditText = findViewById(R.id.notesEditText);
        buttonSubmit = findViewById(R.id.submitButton);
        Gson gson = new GsonBuilder().setLenient().create();

        // Set up logging interceptor for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // OkHttpClient with logging
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        // Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.29.18:8080")
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ContactApi contactApi = retrofit.create(ContactApi.class);

        buttonSubmit.setOnClickListener(v -> submitForm(contactApi));
    }

    private void submitForm(ContactApi contactApi) {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String notes = notesEditText.getText().toString().trim();

        // Check if any field is empty
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || notes.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            emailEditText.setError("Please enter a valid email address");
            return;
        }

        ContactRequest contactRequest = new ContactRequest(firstName, lastName, email, notes);
        contactApi.submitContactForm(contactRequest).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FormActivity.this, "Form submitted successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate to MainActivity after successful form submission
                    Intent intent = new Intent(FormActivity.this, MainActivity.class);
                    startActivity(intent);

                    // Optionally, finish this activity so it cannot be navigated back to
                    finish();
                } else {
                    Toast.makeText(FormActivity.this, "Failed to submit form: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(FormActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidEmail(String email) {
        return pattern.matcher(email).matches();  // Validate the email format
    }

    public static class ContactRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String notes;

        public ContactRequest(String firstName, String lastName, String email, String notes) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.notes = notes;
        }
    }
}