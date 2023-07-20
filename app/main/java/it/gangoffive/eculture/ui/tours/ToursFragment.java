package it.gangoffive.eculture.ui.tours;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import it.gangoffive.eculture.ExportCsvActivity;
import it.gangoffive.eculture.MainActivity;
import it.gangoffive.eculture.R;
import it.gangoffive.eculture.WizardActivity;
import it.gangoffive.eculture.model.TourModel;
import it.gangoffive.eculture.ui.tours.adapters.TourCardRecyclerViewAdapter;
import it.gangoffive.eculture.viewmodel.TourViewModel;

public class ToursFragment extends Fragment {

    private TourViewModel toursViewModel;
    private String userRole;
    private String userUid;
    private TextWatcher watcher;

    private TabLayout.OnTabSelectedListener tabListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        toursViewModel = new ViewModelProvider(this).get(TourViewModel.class);

        // Inflate the layout for this fragment with the TourGrid theme
        View view = inflater.inflate(R.layout.fragment_list_tours, container, false);

        userRole = ((MainActivity) getActivity()).getUserRole();
        userUid = ((MainActivity) getActivity()).getUserUid();

        // Set up the RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1, GridLayoutManager.VERTICAL, false));

        toursViewModel.getTours("", "", "").observe(getViewLifecycleOwner(), new Observer<List<TourModel>>() {
            @Override
            public void onChanged(@Nullable List<TourModel> list) {
                if (list.size() == 0){
                    list.add(new TourModel());
                }
                TourCardRecyclerViewAdapter adapter = new TourCardRecyclerViewAdapter(list);
                recyclerView.setAdapter(adapter);

                //Listener per creazioni  percorsi
                FloatingActionButton buttonActivityTours = getActivity().findViewById(R.id.addPointOfInterestPlaces);
                buttonActivityTours.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), WizardActivity.class);
                        startActivity(intent);
                    }
                });

                if (userRole.equals("curator")){
                    TabLayout tabs = getActivity().findViewById(R.id.tour_tab_container);
                    TextInputLayout searchField = getActivity().findViewById(R.id.search_tours_container);
                    tabs.setVisibility(View.GONE);
                    searchField.setVisibility(View.GONE);
                }
            }
        });

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!userRole.equals("curator")){
            __initInputSearchListener();
            __initTabSelectionListener();
            toursViewModel.loadTours("", "", "");
        } else {
            toursViewModel.loadTours("", "curator", userUid);
        }

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        checkForInternetConnection();

        if (!userRole.equals("curator")){
            __initInputSearchListener();
            __initTabSelectionListener();
            toursViewModel.loadTours("", "", "");
        } else {
            toursViewModel.loadTours("", "curator", userUid);
        }

    }

    @Override
    public void onStop(){
        super.onStop();
        if (!userRole.equals("curator")){
            TabLayout tabs = getActivity().findViewById(R.id.tour_tab_container);
            TextInputEditText term = getActivity().findViewById(R.id.search_tours);
            tabs.removeOnTabSelectedListener(tabListener);
            term.removeTextChangedListener(watcher);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.toolbar_tours_menu, menu);
        int bg = userRole != null && userRole.equals("curator") ? R.drawable.curator_bg : R.drawable.tourist_bg;
        ((MainActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), bg));
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(Html.fromHtml("<font color='#FFFFFF'>"+getString(R.string.title_tours)+"</font>", Html.FROM_HTML_MODE_COMPACT));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tour_share:
                Intent intent = new Intent(getActivity(), ExportCsvActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Controllo per connessione internet. Nel caso fosse assente, viene mostrato a schermo un box informativo
    private void checkForInternetConnection(){
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        com.google.android.material.card.MaterialCardView alertBox = getActivity().findViewById(R.id.alertBoxToursFrag);
        if(cm.getActiveNetworkInfo() == null){
            alertBox.setVisibility(View.VISIBLE);
        }
        else {
            alertBox.setVisibility(View.GONE);
        }
    }


    /**
     *
     * Inizializza il Listener sull'input box di ricerca
     *
     */
    private void __initInputSearchListener(){
        TextInputEditText term = getActivity().findViewById(R.id.search_tours);
        watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                toursViewModel.loadTours(charSequence, "", "");
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        term.addTextChangedListener(watcher);
    }


    /**
     *
     * Inizializza il Listener alla selezione del Tab
     *
     */
    private void __initTabSelectionListener(){
        TabLayout tabs = getActivity().findViewById(R.id.tour_tab_container);
        tabListener = new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextInputLayout searchInput = getActivity().findViewById(R.id.search_tours_container);
                if (String.valueOf(tab.getText()).equals(getActivity().getString(R.string.tour_tab_all))){
                    searchInput.setVisibility(View.VISIBLE);
                    toursViewModel.loadTours("", "", "");
                } else {
                    searchInput.setVisibility(View.GONE);
                    toursViewModel.loadTours("", "curator", userUid);
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        };
        tabs.addOnTabSelectedListener(tabListener);
    }

}