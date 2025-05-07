package com.example.plantique;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AIActivity extends AppCompatActivity {

    private static final String GEMINI_API_KEY = "AIzaSyC3jmaiBjBAPMvuCL13s8UdbL32Ku1CKGk";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    private EditText userInput;
    private FloatingActionButton sendButton;
    private ChipGroup suggestionsContainer;
    private ImageView btnBack;

    private OkHttpClient client;

    private List<String> initialSuggestions = Arrays.asList(
            "Cara menanam tomat di pot",
            "Tips perawatan tanaman hias indoor",
            "Mengatasi hama pada tanaman",
            "Cara menyiram tanaman dengan benar",
            "Jenis pupuk terbaik untuk tanaman buah"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        userInput = findViewById(R.id.userInput);
        sendButton = findViewById(R.id.sendButton);
        suggestionsContainer = findViewById(R.id.suggestionsContainer);
        btnBack = findViewById(R.id.btn_back);

        // Set back button click listener
        btnBack.setOnClickListener(v -> {
            finish(); // Kembali ke halaman sebelumnya
        });

        // Initialize chat recycler view
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Initialize OkHttp client
        client = new OkHttpClient();

        // Set send button click listener
        sendButton.setOnClickListener(v -> handleUserMessage());

        // Add welcome message
        addBotMessage("Assalamu'alaikum! Selamat datang di GrowlyAI. 🌿\n\n" +
                "Saya adalah ahli tanaman yang siap membantu Anda dengan:\n" +
                "- Tips perawatan tanaman\n" +
                "- Diagnosis masalah tanaman\n" +
                "- Cara menanam yang benar\n" +
                "- Rekomendasi pupuk dan nutrisi\n" +
                "- Dan semua hal terkait tanaman lainnya\n\n" +
                "Silakan tanyakan apapun tentang tanaman Anda!");

        // Display initial suggestions
        displaySuggestions(initialSuggestions);
    }

    private void handleUserMessage() {
        String message = userInput.getText().toString().trim();
        if (message.isEmpty()) return;

        // Add user message to chat
        addUserMessage(message);
        userInput.setText("");

        // Show loading message
        addLoadingMessage();

        // Clear suggestions temporarily
        suggestionsContainer.removeAllViews();

        // Call Gemini API
        callGeminiAPI(message);
    }

    private void addUserMessage(String message) {
        chatMessages.add(new ChatMessage(message, true));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        chatMessages.add(new ChatMessage(message, false));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        scrollToBottom();
    }

    private void addLoadingMessage() {
        chatMessages.add(new ChatMessage("", false, true));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        scrollToBottom();
    }

    private void removeLoadingMessage() {
        for (int i = 0; i < chatMessages.size(); i++) {
            if (chatMessages.get(i).isLoading()) {
                chatMessages.remove(i);
                chatAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    private void scrollToBottom() {
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
    }

    private void callGeminiAPI(String userMessage) {
        try {
            JSONObject jsonBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();

            part.put("text", "Berperan sebagai seorang ahli tanaman profesional yang bernama \"PlantCare Assistant\" dan memiliki pengetahuan luas tentang semua jenis tanaman, perawatan, dan tips berkebun. Berikan jawaban yang akurat, praktis, dan ramah dalam bahasa Indonesia. Selalu berikan saran spesifik dan langkah-langkah konkret. Jika ada pertanyaan yang tidak jelas, minta klarifikasi yang diperlukan. Jangan terlalu panjang, maksimal 3-4 paragraf saja. Ingat bahwa Anda adalah ahli perawatan tanaman.\n\nPertanyaan user: " + userMessage);

            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            jsonBody.put("contents", contents);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        removeLoadingMessage();
                        addBotMessage("Maaf, terjadi kesalahan saat memproses pertanyaan Anda. Silakan coba lagi.");
                        displaySuggestions(initialSuggestions);
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseData);

                            if (jsonResponse.has("candidates") && jsonResponse.getJSONArray("candidates").length() > 0) {
                                JSONObject candidate = jsonResponse.getJSONArray("candidates").getJSONObject(0);
                                JSONObject candidateContent = candidate.getJSONObject("content");
                                JSONArray candidateParts = candidateContent.getJSONArray("parts");
                                String botReply = candidateParts.getJSONObject(0).getString("text");

                                runOnUiThread(() -> {
                                    removeLoadingMessage();
                                    addBotMessage(botReply);

                                    // Generate new suggestions based on context
                                    generateNewSuggestions(userMessage, botReply);
                                });
                            } else {
                                throw new JSONException("Invalid response format");
                            }
                        } catch (JSONException e) {
                            runOnUiThread(() -> {
                                removeLoadingMessage();
                                addBotMessage("Maaf, terjadi kesalahan saat memproses respons. Silakan coba lagi.");
                                displaySuggestions(initialSuggestions);
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            removeLoadingMessage();
                            addBotMessage("Maaf, terjadi kesalahan dengan status: " + response.code());
                            displaySuggestions(initialSuggestions);
                        });
                    }
                }
            });
        } catch (JSONException e) {
            runOnUiThread(() -> {
                removeLoadingMessage();
                addBotMessage("Maaf, terjadi kesalahan saat memproses permintaan. Silakan coba lagi.");
                displaySuggestions(initialSuggestions);
            });
        }
    }

    private void generateNewSuggestions(String userMessage, String botReply) {
        try {
            JSONObject jsonBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();

            part.put("text", "Berdasarkan percakapan ini:\n\nUser: " + userMessage + "\nBot: " + botReply + "\n\nBerikan 3-4 saran pertanyaan lanjutan singkat (maksimal 40 karakter) terkait topik yang sedang dibahas yang mungkin ingin ditanyakan oleh pengguna. Hanya berikan daftar pertanyaan saja tanpa penjelasan tambahan, dipisahkan dengan karakter \"|\". Contoh format output: \"Cara memupuk tanaman tomat|Berapa sering menyiram tomat|Mengatasi daun kuning pada tomat\"");

            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            jsonBody.put("contents", contents);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> displaySuggestions(initialSuggestions));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseData);

                            if (jsonResponse.has("candidates") && jsonResponse.getJSONArray("candidates").length() > 0) {
                                JSONObject candidate = jsonResponse.getJSONArray("candidates").getJSONObject(0);
                                JSONObject candidateContent = candidate.getJSONObject("content");
                                JSONArray candidateParts = candidateContent.getJSONArray("parts");
                                String suggestionsText = candidateParts.getJSONObject(0).getString("text");

                                List<String> newSuggestions = Arrays.asList(suggestionsText.split("\\|"));
                                List<String> trimmedSuggestions = new ArrayList<>();
                                for (String suggestion : newSuggestions) {
                                    trimmedSuggestions.add(suggestion.trim());
                                }

                                runOnUiThread(() -> displaySuggestions(trimmedSuggestions));
                            } else {
                                runOnUiThread(() -> displaySuggestions(initialSuggestions));
                            }
                        } catch (JSONException e) {
                            runOnUiThread(() -> displaySuggestions(initialSuggestions));
                        }
                    } else {
                        runOnUiThread(() -> displaySuggestions(initialSuggestions));
                    }
                }
            });
        } catch (JSONException e) {
            runOnUiThread(() -> displaySuggestions(initialSuggestions));
        }
    }

    private void displaySuggestions(List<String> suggestions) {
        suggestionsContainer.removeAllViews();

        for (String suggestion : suggestions) {
            Chip chip = new Chip(this);
            chip.setText(suggestion);
            chip.setClickable(true);
            chip.setCheckable(false);
            chip.setChipBackgroundColorResource(R.color.suggestionChipBackground);

            chip.setOnClickListener(v -> {
                userInput.setText(suggestion);
                handleUserMessage();
            });

            suggestionsContainer.addView(chip);
        }
    }

    // Chat message model class
    private static class ChatMessage {
        private final String message;
        private final boolean isUser;
        private final boolean isLoading;

        public ChatMessage(String message, boolean isUser) {
            this.message = message;
            this.isUser = isUser;
            this.isLoading = false;
        }

        public ChatMessage(String message, boolean isUser, boolean isLoading) {
            this.message = message;
            this.isUser = isUser;
            this.isLoading = isLoading;
        }

        public String getMessage() {
            return message;
        }

        public boolean isUser() {
            return isUser;
        }

        public boolean isLoading() {
            return isLoading;
        }
    }

    // Chat adapter class
    private class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_USER = 1;
        private static final int VIEW_TYPE_BOT = 2;
        private static final int VIEW_TYPE_LOADING = 3;

        private final List<ChatMessage> messages;

        public ChatAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            ChatMessage message = messages.get(position);
            if (message.isLoading()) {
                return VIEW_TYPE_LOADING;
            } else if (message.isUser()) {
                return VIEW_TYPE_USER;
            } else {
                return VIEW_TYPE_BOT;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            switch (viewType) {
                case VIEW_TYPE_USER:
                    View userView = inflater.inflate(R.layout.item_user_message, parent, false);
                    return new UserMessageViewHolder(userView);
                case VIEW_TYPE_BOT:
                    View botView = inflater.inflate(R.layout.item_bot_message, parent, false);
                    return new BotMessageViewHolder(botView);
                case VIEW_TYPE_LOADING:
                    View loadingView = inflater.inflate(R.layout.item_loading, parent, false);
                    return new LoadingViewHolder(loadingView);
                default:
                    throw new IllegalArgumentException("Unknown view type");
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ChatMessage message = messages.get(position);

            if (holder instanceof UserMessageViewHolder) {
                ((UserMessageViewHolder) holder).bind(message);
            } else if (holder instanceof BotMessageViewHolder) {
                ((BotMessageViewHolder) holder).bind(message);
            }
            // No binding needed for loading view holder
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class UserMessageViewHolder extends RecyclerView.ViewHolder {
            private final TextView messageText;

            public UserMessageViewHolder(@NonNull View itemView) {
                super(itemView);
                messageText = itemView.findViewById(R.id.messageText);
            }

            public void bind(ChatMessage message) {
                messageText.setText(message.getMessage());
            }
        }

        class BotMessageViewHolder extends RecyclerView.ViewHolder {
            private final TextView messageText;

            public BotMessageViewHolder(@NonNull View itemView) {
                super(itemView);
                messageText = itemView.findViewById(R.id.messageText);
            }

            public void bind(ChatMessage message) {
                messageText.setText(message.getMessage());
            }
        }

        class LoadingViewHolder extends RecyclerView.ViewHolder {
            public LoadingViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}