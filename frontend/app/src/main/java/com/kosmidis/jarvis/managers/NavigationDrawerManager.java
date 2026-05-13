package com.kosmidis.jarvis.managers;

import android.view.Menu;
import android.view.SubMenu;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.kosmidis.jarvis.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class NavigationDrawerManager {

    public interface DrawerCallback {
        void onNewChatClicked();
        void onSettingsClicked();
        void onLogoutClicked();
        void onConversationClicked(int conversationId);
    }

    private final DrawerLayout drawerLayout;
    private final Toolbar toolbar;
    private final NavigationView navigationView;

    public NavigationDrawerManager(
            DrawerLayout drawerLayout,
            Toolbar toolbar,
            NavigationView navigationView
    ) {
        this.drawerLayout = drawerLayout;
        this.toolbar = toolbar;
        this.navigationView = navigationView;
    }

    public void setup(String userEmail, DrawerCallback callback) {
        setupHeaderEmail(userEmail);
        setupToolbar();
        setupMenu(callback);
    }

    private void setupHeaderEmail(String userEmail) {
        android.view.View headerView = navigationView.getHeaderView(0);
        TextView tvHeaderEmail = headerView.findViewById(R.id.tvHeaderEmail);

        if (tvHeaderEmail != null) {
            tvHeaderEmail.setText(userEmail);
        }
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );
    }

    private void setupMenu(DrawerCallback callback) {
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);

            if (item.getItemId() == R.id.nav_new_chat) {
                callback.onNewChatClicked();
            } else if (item.getItemId() == R.id.nav_settings) {
                callback.onSettingsClicked();
            } else if (item.getItemId() == R.id.nav_logout) {
                callback.onLogoutClicked();
            } else {
                callback.onConversationClicked(item.getItemId());
            }

            return true;
        });
    }

    public void updateConversationHistory(JSONArray jsonArray) {
        Menu menu = navigationView.getMenu();
        android.view.MenuItem historyItem = menu.findItem(R.id.nav_history_parent);

        if (historyItem == null || historyItem.getSubMenu() == null) {
            return;
        }

        SubMenu subMenu = historyItem.getSubMenu();
        subMenu.clear();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject chatObj = jsonArray.getJSONObject(i);
                int convId = chatObj.getInt("id");
                String title = chatObj.getString("title");

                subMenu.add(R.id.group_history, convId, Menu.NONE, title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}