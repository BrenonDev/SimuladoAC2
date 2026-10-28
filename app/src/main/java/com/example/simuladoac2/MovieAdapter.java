package com.example.simuladoac2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private final List<Movie> movies;
    private final OnMovieClickListener clickListener;
    private final OnMovieClickListener longClickListener;

    public MovieAdapter(
            List<Movie> movies,
            OnMovieClickListener clickListener,
            OnMovieClickListener longClickListener
    ) {
        this.movies = movies;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.name.setText(movie.getName());
        holder.details.setText(holder.itemView.getContext().getString(
                R.string.movie_details,
                movie.getType(),
                movie.getGenre(),
                movie.getReleaseYear()
        ));
        holder.rating.setText(holder.itemView.getContext().getString(
                R.string.movie_rating,
                String.format(Locale.getDefault(), "%.1f", movie.getRating())
        ));
        holder.watched.setText(movie.isWatched()
                ? R.string.status_watched
                : R.string.status_not_watched);

        holder.itemView.setOnClickListener(view -> clickListener.onMovieClick(movie));
        holder.itemView.setOnLongClickListener(view -> {
            longClickListener.onMovieClick(movie);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView details;
        private final TextView rating;
        private final TextView watched;

        MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textMovieName);
            details = itemView.findViewById(R.id.textMovieDetails);
            rating = itemView.findViewById(R.id.textMovieRating);
            watched = itemView.findViewById(R.id.textMovieWatched);
        }
    }
}
