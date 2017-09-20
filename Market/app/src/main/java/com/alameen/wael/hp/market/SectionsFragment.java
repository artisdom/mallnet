package com.alameen.wael.hp.market;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridLayout;

import java.util.ArrayList;
import java.util.List;


public class SectionsFragment extends Fragment implements AdapterView.OnItemClickListener {

    RecyclerView sectionsRecycler;
    RecyclerView.Adapter adapter;
    List<Sections> sectionsList = new ArrayList<>();
    private static int lastFirstVisiblePosition;

    public SectionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_sections, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addItems();
        adapter = new SecRecyclerAdapter(getContext(), sectionsList, this);
        sectionsRecycler = (RecyclerView)view.findViewById(R.id.sections_recycler);
        sectionsRecycler.setHasFixedSize(true);
        sectionsRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false));
        sectionsRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void addItems() {
        String[] secTitles = getResources().getStringArray(R.array.sec_item_titles);
        int i = 0;

        int[] images = { R.drawable.clothes, R.drawable.accessories, R.drawable.shoesbacks, R.drawable.perfumes, R.drawable.electrics, R.drawable.electronics,
                R.drawable.presents, R.drawable.books, R.drawable.makeups, R.drawable.atheltics, R.drawable.cheap};

        for (String title : secTitles) {
            Sections object = new Sections(title, images[i]);
            sectionsList.add(object);
            i++;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //Toast.makeText(getContext(), "you select "+sectionsList.get(i).getSecTitle(), Toast.LENGTH_SHORT).show();
        if(MainActivity.isOpened) {
            view.setClickable(false);
        } else {
            view.setClickable(true);
            if(sectionsList.get(i).getSecTitle().equals(getString(R.string.accessories))) {
                startActivity(new Intent(getActivity(), AccessoriesActivity.class));

            } else if(sectionsList.get(i).getSecTitle().equals(getString(R.string.clothes))) {
                startActivity(new Intent(getActivity(), ClothesActivity.class));

            } else if(sectionsList.get(i).getSecTitle().equals(getString(R.string.shoes_and_backs))) {
                    startActivity(new Intent(getActivity(), ShoesAndBagsActivity.class));

            } else if(sectionsList.get(i).getSecTitle().equals(getString(R.string.perfumes))) {
                startActivity(new Intent(getActivity(), PerfumesActivity.class));

            } else if(sectionsList.get(i).getSecTitle().equals(getString(R.string.electrics))) {
                startActivity(new Intent(getActivity(), ElectricsActivity.class));

            } else if(sectionsList.get(i).getSecTitle().equals(getString(R.string.electronics))) {
                startActivity(new Intent(getActivity(), ElectronicsActivity.class));

            } else if(sectionsList.get(i).getSecTitle().equals(getString(R.string.stationary))) {
                startActivity(new Intent(getActivity(), StationaryActivity.class));

            } else if(sectionsList.get(i).getSecTitle().equals(getString(R.string.books_magazines))) {
                startActivity(new Intent(getActivity(), BooksActivity.class));

            } else if(sectionsList.get(i).getSecTitle().equals(getString(R.string.make_ups))) {
                startActivity(new Intent(getActivity(), MakeupsActivity.class));

            } else if(sectionsList.get(i).getSecTitle().equals(getString(R.string.athletics_tools))) {
                startActivity(new Intent(getActivity(), AthleticsActivity.class));

            } else {
                startActivity(new Intent(getActivity(), QuarterActivity.class));
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        lastFirstVisiblePosition = ((GridLayoutManager) sectionsRecycler.getLayoutManager()).findFirstVisibleItemPosition();
        Log.d("pos", Integer.toString(lastFirstVisiblePosition));
    }

    @Override
    public void onResume() {
        super.onResume();

        (sectionsRecycler.getLayoutManager()).scrollToPosition(lastFirstVisiblePosition);
        Log.d("pos", Integer.toString(lastFirstVisiblePosition));
    }
}
