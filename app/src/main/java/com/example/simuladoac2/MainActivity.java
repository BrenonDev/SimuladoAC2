package com.example.simuladoac2;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final List<Movie> movies = new ArrayList<>();
    private final List<Movie> filteredMovies = new ArrayList<>();

    private MovieRepository repository;
    private MovieAdapter adapter;
    private ListenerRegistration moviesListener;
    private EditText nameInput;
    private EditText yearInput;
    private EditText ratingInput;
    private Spinner typeSpinner;
    private Spinner genreSpinner;
    private Spinner typeFilterSpinner;
    private CheckBox watchedInput;
    private CheckBox watchedFilter;
    private EditText searchInput;
    private Button saveButton;
    private Button cancelButton;
    private TextView emptyState;
    private String editingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new MovieRepository();
        bindViews();
        configureSpinners();
        configureMovieList();
        configureFilters();
        configureForm();
    }

    @Override
    protected void onStart() {
        super.onStart();
        moviesListener = repository.observeMovies(this::onMoviesChanged);
    }

    @Override
    protected void onStop() {
        if (moviesListener != null) {
            moviesListener.remove();
            moviesListener = null;
        }
        super.onStop();
    }

    private void bindViews() {
        nameInput = findViewById(R.id.inputName);
        yearInput = findViewById(R.id.inputYear);
        ratingInput = findViewById(R.id.inputRating);
        typeSpinner = findViewById(R.id.spinnerType);
        genreSpinner = findViewById(R.id.spinnerGenre);
        typeFilterSpinner = findViewById(R.id.spinnerTypeFilter);
        watchedInput = findViewById(R.id.checkboxWatched);
        watchedFilter = findViewById(R.id.checkboxWatchedFilter);
        searchInput = findViewById(R.id.inputSearch);
        saveButton = findViewById(R.id.buttonSave);
        cancelButton = findViewById(R.id.buttonCancel);
        emptyState = findViewById(R.id.textEmptyState);
    }

    private void configureSpinners() {
        setSpinnerAdapter(typeSpinner, R.array.movie_types);
        setSpinnerAdapter(genreSpinner, R.array.movie_genres);
        setSpinnerAdapter(typeFilterSpinner, R.array.movie_type_filters);
    }

    private void setSpinnerAdapter(Spinner spinner, int arrayResource) {
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                arrayResource,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
    }

    private void configureMovieList() {
        RecyclerView recyclerView = findViewById(R.id.recyclerMovies);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MovieAdapter(filteredMovies, this::startEditing, this::confirmDelete);
        recyclerView.setAdapter(adapter);
    }

    private void configureFilters() {
        typeFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    AdapterView<?> parent,
                    View view,
                    int position,
                    long id
            ) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                applyFilters();
            }
        });
        watchedFilter.setOnCheckedChangeListener((button, checked) -> applyFilters());
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable text) {
            }
        });
    }

    private void configureForm() {
        saveButton.setOnClickListener(view -> saveMovie());
        cancelButton.setOnClickListener(view -> clearForm());
    }

    private void onMoviesChanged(
            List<DocumentSnapshot> documents,
            FirebaseFirestoreException error
    ) {
        if (error != null) {
            showMessage(getString(R.string.error_loading_movies, error.getLocalizedMessage()));
            return;
        }

        movies.clear();
        for (DocumentSnapshot document : documents) {
            Movie movie = document.toObject(Movie.class);
            if (movie != null) {
                movie.setId(document.getId());
                movies.add(movie);
            }
        }
        applyFilters();
    }

    private void applyFilters() {
        String selectedType = typeFilterSpinner.getSelectedItemPosition() == 0
                ? null
                : typeFilterSpinner.getSelectedItem().toString();
        String searchText = searchInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        boolean onlyWatched = watchedFilter.isChecked();

        filteredMovies.clear();
        for (Movie movie : movies) {
            boolean matchesType = selectedType == null || selectedType.equals(movie.getType());
            boolean matchesName = movie.getName() != null
                    && movie.getName().toLowerCase(Locale.ROOT).contains(searchText);
            boolean matchesWatched = !onlyWatched || movie.isWatched();
            if (matchesType && matchesName && matchesWatched) {
                filteredMovies.add(movie);
            }
        }

        adapter.notifyDataSetChanged();
        emptyState.setVisibility(filteredMovies.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void saveMovie() {
        String name = nameInput.getText().toString().trim();
        String yearText = yearInput.getText().toString().trim();
        String ratingText = ratingInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError(getString(R.string.error_required_name));
            nameInput.requestFocus();
            return;
        }
        if (typeSpinner.getSelectedItemPosition() == 0) {
            showMessage(getString(R.string.error_select_type));
            return;
        }
        if (genreSpinner.getSelectedItemPosition() == 0) {
            showMessage(getString(R.string.error_select_genre));
            return;
        }
        if (yearText.isEmpty()) {
            yearInput.setError(getString(R.string.error_required_year));
            yearInput.requestFocus();
            return;
        }
        if (ratingText.isEmpty()) {
            ratingInput.setError(getString(R.string.error_required_rating));
            ratingInput.requestFocus();
            return;
        }

        int releaseYear;
        double rating;
        try {
            releaseYear = Integer.parseInt(yearText);
            rating = Double.parseDouble(ratingText.replace(',', '.'));
        } catch (NumberFormatException exception) {
            showMessage(getString(R.string.error_invalid_numbers));
            return;
        }

        if (rating < 0 || rating > 10) {
            ratingInput.setError(getString(R.string.error_rating_range));
            ratingInput.requestFocus();
            return;
        }

        Movie movie = new Movie(
                name,
                typeSpinner.getSelectedItem().toString(),
                genreSpinner.getSelectedItem().toString(),
                releaseYear,
                rating,
                watchedInput.isChecked()
        );

        if (editingId == null) {
            repository.add(movie)
                    .addOnSuccessListener(document -> {
                        showMessage(getString(R.string.movie_saved));
                        clearForm();
                    })
                    .addOnFailureListener(error -> showMessage(
                            getString(R.string.error_saving_movie, error.getLocalizedMessage())
                    ));
        } else {
            repository.update(editingId, movie)
                    .addOnSuccessListener(unused -> {
                        showMessage(getString(R.string.movie_updated));
                        clearForm();
                    })
                    .addOnFailureListener(error -> showMessage(
                            getString(R.string.error_updating_movie, error.getLocalizedMessage())
                    ));
        }
    }

    private void startEditing(Movie movie) {
        editingId = movie.getId();
        nameInput.setText(movie.getName());
        selectSpinnerValue(typeSpinner, movie.getType());
        selectSpinnerValue(genreSpinner, movie.getGenre());
        yearInput.setText(String.valueOf(movie.getReleaseYear()));
        ratingInput.setText(String.format(Locale.getDefault(), "%s", movie.getRating()));
        watchedInput.setChecked(movie.isWatched());
        saveButton.setText(R.string.action_update);
        cancelButton.setVisibility(View.VISIBLE);
    }

    private void selectSpinnerValue(Spinner spinner, String value) {
        for (int position = 0; position < spinner.getCount(); position++) {
            if (value.equals(spinner.getItemAtPosition(position).toString())) {
                spinner.setSelection(position);
                return;
            }
        }
    }

    private void clearForm() {
        editingId = null;
        nameInput.setText("");
        typeSpinner.setSelection(0);
        genreSpinner.setSelection(0);
        yearInput.setText("");
        ratingInput.setText("");
        watchedInput.setChecked(false);
        nameInput.setError(null);
        yearInput.setError(null);
        ratingInput.setError(null);
        saveButton.setText(R.string.action_save);
        cancelButton.setVisibility(View.GONE);
    }

    private void confirmDelete(Movie movie) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_confirmation, movie.getName()))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) ->
                        repository.delete(movie.getId())
                                .addOnSuccessListener(unused -> {
                                    if (movie.getId().equals(editingId)) {
                                        clearForm();
                                    }
                                    showMessage(getString(R.string.movie_deleted));
                                })
                                .addOnFailureListener(error -> showMessage(
                                        getString(R.string.error_deleting_movie, error.getLocalizedMessage())
                                )))
                .show();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
