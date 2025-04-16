package com.example.talkie.navigationbar

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.talkie.component.Home
import com.example.talkie.component.NewChat
import com.example.talkie.component.Profile
import com.example.talkie.datamodels.NavItem

@Composable
fun navBar(navController: NavHostController) {
    val navItemList= listOf(
        NavItem("Home", Icons.Outlined.Home),
        NavItem("New Chat", Icons.Filled.Add),
        NavItem("Profile", Icons.Outlined.AccountCircle),
    )
    var selectedIndex by remember {
        mutableIntStateOf(0)
    }
    Scaffold(modifier=Modifier.fillMaxSize(),
             bottomBar = {
                 NavigationBar(modifier=Modifier.drawWithContent { drawContent()
                 drawLine(color = Color.Gray, start = Offset(0f, 0f), end = Offset(size.width, 0f), strokeWidth = 2f)},
                     containerColor = Color.White,
                     contentColor = Color.LightGray
                 ){
                    navItemList.forEachIndexed { index, navItem ->
                        NavigationBarItem(selected = selectedIndex==index,
                            onClick = { selectedIndex=index },
                            icon = { Icon(imageVector = navItem.icon, contentDescription = navItem.lable) },
                            label = {if (navItem.lable=="New Chat"){
                                Text(text = navItem.lable, fontSize = 14.sp)} })
                    }
                 }

             }) { paddingValues ->
        ContentScreen(modifier=Modifier.padding(paddingValues), selectedIndex, navController)
    }
}
@Composable
fun ContentScreen(modifier: Modifier, selectedIndex: Int, navController: NavHostController) {
    when(selectedIndex){
        0-> Home(navController, LocalContext.current)
        1-> NewChat(navController, LocalContext.current)
        2-> Profile(navController)
    }
}