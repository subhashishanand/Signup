package com.example.signup;

        import android.content.Intent;
        import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;



        import androidx.cardview.widget.CardView;
        import androidx.fragment.app.Fragment;



/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    CardView pdfCardView;
    CardView imageCardView;





    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        pdfCardView=v.findViewById(R.id.selectPdf);
        imageCardView = v.findViewById(R.id.selectImage);


        imageCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(getActivity(),ImageActivity.class);
                startActivity(intent);
            }
        });

        pdfCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.user == null){
                    ((MainActivity)getActivity()).signIn();
                }else{
                        Intent intent= new Intent(getActivity(),pdfActivity.class);
                        startActivity(intent);
                    }
                }
        });

        return v;
    }
}
