package com.forrestguice.suntimeswidget.welcome;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.forrestguice.suntimeswidget.AboutActivity;
import com.forrestguice.suntimeswidget.R;
import com.forrestguice.support.app.AppCompatActivity;

public class WelcomeLegalView extends WelcomeView
{
    public WelcomeLegalView(Context context) {
        super(context, R.layout.layout_welcome_legal);
    }
    public WelcomeLegalView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.layout_welcome_legal);
    }
    public WelcomeLegalView(AppCompatActivity activity) {
        super(activity, R.layout.layout_welcome_legal);
    }

    public static WelcomeLegalView newInstance(AppCompatActivity activity) {
        return new WelcomeLegalView(activity);
    }

    @Override
    public void initViews(Context context, View view)
    {
        super.initViews(context, view);

        Button aboutButton = (Button) view.findViewById(R.id.button_about);
        if (aboutButton != null) {
            aboutButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAbout(view);
                }
            });
        }
    }

    public void showAbout( View view )
    {
        Activity activity = getActivity();
        if (activity != null) {
            activity.startActivity(new Intent(activity, AboutActivity.class));
            activity.overridePendingTransition(R.anim.transition_next_in, R.anim.transition_next_out);
        }
    }
}
