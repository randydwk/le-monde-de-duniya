import java.util.FormatFlagsConversionMismatchException;

import javax.swing.text.StyleConstants.ColorConstants;

import extensions.CSVFile;
class Duniya extends Program{

    //-----------//
    // CONSTANTS //
    //-----------//

    final String[] PLAYER_SPRITES = new String[]{"..","^.","^^",".^"};
    final String[] COLORS = new String[]{"green","cyan","blue","purple","red","yellow","white","black"};
    final int MAX_CAM_WIDTH = 32;  // The max visible width
    final int MAX_CAM_HEIGHT = 18; // The max visible height
    final int FIGHT_WIDTH = 182;
    // CSV Files
    final String[][] worlds_list = csvLoad("worlds_list.csv",1);
    final String[][] npc_dialogs = csvLoad("npc_dialogs.csv",1);
    final String[][] wizard_dialogs = csvLoad("wizard_dialogs.csv",1);
    final String[][] questions = csvLoad("questions.csv",1);
    // Create Types
    Player player = newPlayer(2,2);
    Camera cam = newCamera(0,0);

    //--------------//
    // MAIN PROGRAM //
    //--------------//

    void algorithm(){
        // World Initialisation
        String world_current = "house_hero_2";
        int[] worlds_x = generateIntTable(length(worlds_list,2),-1);
        int[] worlds_y = generateIntTable(length(worlds_list,2),-1);
        String[][] world = csvLoad(world_current+".csv",5);
        String world_name = worlds_list[1][tabGetIndex(worlds_list[0],world_current)];
        
        // NPC & Wizards
        int[] wizard_step = generateIntTable(length(wizard_dialogs,2)-6,0);
        String last_wizard = "";
        boolean last_answer_good = true;
        
        // Fragments & Khoïd Door
        int[] fragments = new int[]{0,0,0}; // Khäna, Ömnöd and Baïna Fragments
        int game_step = 0;

        // Fights & Enemies
        int[] enemies = new int[]{0,0,0}; // Khäna, Ömnöd and Baïna Enemies
        int random_fight = 30;
        int player_steps = 0;

        String menu = "main";
        String key = "";
        while (menu != "end"){

            //-----------//
            // MAIN MENU //
            //-----------//

            if (equals(menu,"main")){
                key = menuMain();
                switch(key){
                    case "N":
                        // Reinitialisation
                        world_current = "house_hero_2";
                        worlds_x = generateIntTable(length(worlds_list,2),-1);
                        worlds_y = generateIntTable(length(worlds_list,2),-1);
                        world = csvLoad(world_current+".csv",5);
                        world_name = worlds_list[1][tabGetIndex(worlds_list[0],world_current)];
                        wizard_step = generateIntTable(length(wizard_dialogs,2)-6,0);
                        fragments = new int[]{0,0,0};
                        enemies = new int[]{0,0,0};
                        game_step = 0;
                        last_wizard = "";
                        last_answer_good = true;
                        random_fight = 30;
                        player_steps = 0;
                        player.spriteindex = 2;
                        player.dir[0] = 0;
                        player.dir[1] = 1;
                        movePlayerToSpawn(world,player);
                        cam.vw = 0;
                        cam.vh = 0;
                        // Introduction
                        for (int i = 0; i < 3; i++){
                            menuNPC(npc_dialogs,cam,world,player,"Introduction",fragments,"Le Monde de Duniya ("+(i+1)+"/3)");
                        }
                        menuNPC(npc_dialogs,cam,world,player,"Introduction",fragments,"L’apparition du Héros");
                        menu = "game";
                    break;
                    case "C": menu = "load"; break;
                    case "Q": menu = "end"; break;
                }
            }

            //-----------//
            // LOAD MENU //
            //-----------//

            else if (equals(menu,"load")){
                key = menuLoad();
                if (equals(key,"R")){
                    menu = "main";
                } else {
                    String[] loadtemp = loadGame(key,worlds_x,worlds_y,wizard_step,fragments,enemies,player);
                    world_current = loadtemp[0];
                    game_step = stringToInt(loadtemp[1]);
                    world = csvLoad(world_current+".csv",5);
                    world_name = worlds_list[1][tabGetIndex(worlds_list[0],world_current)];
                    last_wizard = "";
                    last_answer_good = true;
                    random_fight = 30;
                    player_steps = 0;
                    menu = "game";
                }
            }

            //-----------//
            // SAVE MENU //
            //-----------//

            else if (equals(menu,"save")){
                key = menuSave();
                if (equals(key,"R")){
                    menu = "game";
                } else if (equals(key,"M")){
                    menu = "main";
                } else {
                    saveGame(key,worlds_x,worlds_y,wizard_step,fragments,enemies,world_current,game_step,player);
                    menu = "gamesaved";
                }
            }

            else if (equals(menu,"gamesaved")){
                menuGameSaved();
                menu = "save";
            }

            //---------------//
            // MAIN GAMEPLAY //
            //---------------//
            
            else if (equals(menu,"game")){
                // Change zone when possible
                String place = world[player.y][player.x];
                if (charAt(place,4) == '2'){
                    if (equals(world_current,"duniya_plain_open")){
                        playerSavePosition(worlds_list[0],worlds_x,worlds_y,"duniya_plain",player);
                    } else if (equals(world_current,"village_north_end")){
                        playerSavePosition(worlds_list[0],worlds_x,worlds_y,"village_north",player);
                    } else {
                        playerSavePosition(worlds_list[0],worlds_x,worlds_y,world_current,player);
                    }
                    world_current = substring(place,5,length(place));
                    world = csvLoad(world_current+".csv",5);
                    playerGotoSavedPosition(world,worlds_list[0],worlds_x,worlds_y,world_current,player);
                    camFollowPlayer(cam,world,player);
                }

                // Change zone if the Khoïd Door is opened
                if (game_step >= 1 && equals(world_current,"duniya_plain")){
                    playerSavePosition(worlds_list[0],worlds_x,worlds_y,"duniya_plain",player);
                    world_current = "duniya_plain_open";
                    world = csvLoad("duniya_plain_open.csv",5);
                    playerGotoSavedPosition(world,worlds_list[0],worlds_x,worlds_y,"duniya_plain",player);
                    camFollowPlayer(cam,world,player);
                }

                // Change zone if the game is finished
                if (game_step == 2 && equals(world_current,"village_north")){
                    playerSavePosition(worlds_list[0],worlds_x,worlds_y,"village_north",player);
                    world_current = "village_north_end";
                    world = csvLoad("village_north_end.csv",5);
                    playerGotoSavedPosition(world,worlds_list[0],worlds_x,worlds_y,"village_north",player);
                    camFollowPlayer(cam,world,player);
                }

                // Draw on screen
                camFollowPlayer(cam,world,player);
                world_name = worlds_list[1][tabGetIndex(worlds_list[0],world_current)];
                drawWorld(cam,world,player,world_name);
                menuActions(fragments,canInteract(world,player));

                // Get the player input
                key = keyboardInput();
                if (equals(key,"A") && canInteract(world,player)){ // Interact
                    String npc_place = world[player.y+player.dir[1]][player.x+player.dir[0]];
                    String npc_name = substring(npc_place,5,length(npc_place));
                    if (equals(npc_name,"Vilsvik")){ // Boss Fight
                        menu = "bossfight";
                    } else {
                        if (equals(npc_name,"Porte de Khoïd") && fragments[0]+fragments[1]+fragments[2] == 15){ // Khoïd Door
                            npc_name = "Dieu Hikami";
                            game_step = 1;
                        }
                        if (isMage(npc_name)){
                            int answer = menuWizard(wizard_dialogs,cam,world,world_current,player,world_name,fragments,npc_name,wizard_step,last_wizard,questions,last_answer_good);
                            if (answer == 0){
                                last_answer_good = false;
                            } else if (answer == 1){
                                last_answer_good = true;
                            }
                            if (npc_name != last_wizard){
                                last_wizard = npc_name;
                            }
                        } else {
                            menuNPC(npc_dialogs,cam,world,player,world_name,fragments,npc_name);
                        }
                    }
                } else if (equals(key,"M")){ // Menu
                    menu = "save";
                } else {
                    keyboardMovePlayer(key,world,player);
                    if (currentVillage(world_current) != -1 && currentVillage(world_current) != 3
                    && fragments[0]+fragments[1]+fragments[2] < 15
                    && enemies[currentVillage(world_current)] < 4){ // Fights
                        player_steps += 1;
                        if (player_steps == random_fight){
                            random_fight = 100+(int) (random()*100);
                            player_steps = 0;
                            menu = "fight";
                        }
                    }
                }
            }

            //--------//
            // FIGHTS //
            //--------//

            else if (equals(menu,"fight")){
                enemyFight(enemies,fragments,world_current);
                menu = "game";
            } else if (equals(menu,"bossfight")){
                if (enemyFight(enemies,fragments,world_current)){
                    game_step = 2;
                    menuNPC(npc_dialogs,cam,world,player,world_name,fragments,"Mission Accomplie");
                }
                menu = "game";
            }
        }
        clearScreen();
        cursor(0,0);
        // Game Quit
    }

    //-----------------//
    // MENUS FUNCTIONS //
    //-----------------//

    String menuMain(){
        String res;
        do {
            clearScreen();  
            cursor(0,0);
            printTextboxBorder(64);
            printTextbox("","white",64);
            printTextbox("LE MONDE DE","green",64);
            printTextbox(" ____   _   _  _   _  ___ __   __  _    ","cyan",64);
            printTextbox("|  _ \\ | | | || \\ | ||_ _|\\ \\ / / / \\   ","cyan",64);
            printTextbox("| | | || | | ||  \\| | | |  \\ V / / _ \\  ","blue",64);
            printTextbox("| |_| || |_| || |\\  | | |   | | / ___ \\ ","blue",64);
            printTextbox("|____/  \\___/ |_| \\_||___|  |_|/_/   \\_\\","purple",64);
            printTextbox("","white",64);
            printTextbox("N - Nouvelle partie","white",64);
            printTextbox("C - Charger une partie","white",64);
            printTextbox("Q - Quitter le jeu","white",64);
            printTextbox("Entrée - Valider","white",64);
            printTextbox("","white",64);
            printTextboxBorder(64);
            res = keyboardInput();
        } while (!equals(res,"N") && !equals(res,"C") && !equals(res,"Q"));
        return res;
    }

    String menuLoad(){
        String res;
        do {
            clearScreen();  
            cursor(0,0);
            printTextboxBorder(64);
            printTextbox("","white",64);
            printTextbox("Charger une partie","yellow",64);
            printTextbox("","white",64);
            printTextbox("","white",64);
            printTextbox("1 - Charger l'emplacement 1","white",64);
            printTextbox("2 - Charger l'emplacement 2","white",64);
            printTextbox("3 - Charger l'emplacement 3","white",64);
            printTextbox("","white",64);
            printTextbox("","white",64);
            printTextbox("","white",64);
            printTextbox("R - Retour au menu","white",64);
            printTextbox("Entrée - Valider","white",64);
            printTextbox("","white",64);
            printTextboxBorder(64);
            res = keyboardInput();
        } while (!equals(res,"1") && !equals(res,"2") && !equals(res,"3") && !equals(res,"R"));
        return res;
    }

    String menuSave(){
        String res;
        do {
            clearScreen();  
            cursor(0,0);
            printTextboxBorder(64);
            printTextbox("","white",64);
            printTextbox("Sauvegarder la partie","yellow",64);
            printTextbox("","white",64);
            printTextbox("","white",64);
            printTextbox("1 - Sauvegarder sur l'emplacement 1","white",64);
            printTextbox("2 - Sauvegarder sur l'emplacement 2","white",64);
            printTextbox("3 - Sauvegarder sur l'emplacement 3","white",64);
            printTextbox("","white",64);
            printTextbox("R - Retour au jeu","white",64);
            printTextbox("M - Menu Principal","white",64);
            printTextbox("(quitte sans sauvegarder)","white",64);
            printTextbox("Entrée - Valider","white",64);
            printTextbox("","white",64);
            printTextboxBorder(64);
            res = keyboardInput();
        } while (!equals(res,"1") && !equals(res,"2") && !equals(res,"3") && !equals(res,"R") && !equals(res,"M"));
        return res;
    }

    void menuGameSaved(){
        clearScreen();  
        cursor(0,0);
        printTextboxBorder(64);
        printTextbox("","white",64);
        printTextbox("Sauvegarder la partie","yellow",64);
        printTextbox("","white",64);
        printTextbox("","white",64);
        printTextbox("","white",64);
        printTextbox("Sauvegarde effectuée !","white",64);
        printTextbox("","white",64);
        printTextbox("","white",64);
        printTextbox("","white",64);
        printTextbox("","white",64);
        printTextbox("","white",64);
        printTextbox("Entrée - OK","white",64);
        printTextbox("","white",64);
        printTextboxBorder(64);
        keyboardInput();
    }

    void drawWorld(Camera cam, String[][] world, Player player, String world_name){
        clearScreen();  
        cursor(0,0);
        text("yellow");
        println(stringCenter(world_name,MAX_CAM_WIDTH*2));
        for (int lig = cam.y; lig < cam.y+cam.vh; lig++){
            text("black");
            print(stringAddSpaces("",(MAX_CAM_WIDTH-cam.vw)));
            for (int col = cam.x; col < cam.x+cam.vw; col++){
                if (player.x == col && player.y == lig){
                    background("black");
                    text("green");
                    print(PLAYER_SPRITES[player.spriteindex]);
                } else {
                    background(COLORS[charAt(world[lig][col],0)-'0']);
                    if (charAt(world[lig][col],1) != ' '){
                        text(COLORS[charAt(world[lig][col],1)-'0']);
                    }
                    print(substring(world[lig][col],2,4));
                }
            }
            background("black");
            println();
        }
    }

    void menuActions(int[] fragments, boolean interact){
        int fragnb = fragments[0]+fragments[1]+fragments[2];
        text("white");
        printTextboxBorder(64);
        print("| ");
        text("yellow");
        print("FRAGMENTS (");
        print(stringAddSpaces(fragnb+"/15)",6));
        print("                                  COMMANDES");
        text("white");
        println(" |");
        if (fragnb < 15){
            print("| Khäna : ");
            drawFragments(fragments[0],"blue");
            print("          Changer Direction : Z Q S D |\n| Ömnöd : ");
            drawFragments(fragments[1],"green");
            print("                       Menu : M       |\n| Baïna : ");
            drawFragments(fragments[2],"red");
        } else {
            print("| ");
            text("purple");
            print("/\\/\\/\\");
            text("white");
            println("                           Changer Direction : Z Q S D |");
            print("| ");
            text("purple");
            print("\\\\\\/// LA PIERRE DIVINE");
            text("white");
            println("                       Menu : M       |");
            print("| ");
            text("purple");
            print(" \\\\//                  ");
        }
        text("white");
        println("          Avancer / Valider : Entrée  |");
        if (interact){
            print("|                                          ");
            text("yellow");
            print("Intéragir : A");
            text("white");
            println("       |");
        }
        printTextboxBorder(64);
    }

    void drawFragments(int fragnb, String color){
        text(color);
        for (int i = 0; i < 5; i++){
            if (i < fragnb){
                print("<> ");
            } else {
                print("   ");
            }
        }
        text("white");
    }   

    boolean enemyFight(int[] enemies, int[] fragments, String world_current){
        background("white");
        boolean win = false;
        String enemy_name = "";
        int score_min = 6;
        int phases_nb = 3;
        switch (currentVillage(world_current)){
            case 0: enemy_name = "enemy_west_"+enemies[0];  enemies[0] += 1; break;
            case 1: enemy_name = "enemy_south_"+enemies[1]; enemies[1] += 1; break;
            case 2: enemy_name = "enemy_east_"+enemies[2];  enemies[2] += 1; break;
            case 3: enemy_name = "enemy_vilsvik";           score_min = 9;   break;
        }
        if (equals(enemy_name,"enemy_south_0")){
            phases_nb = 1;
        }
        String[][] enemy = csvLoad(enemy_name+".csv",1);
        int score = 0;
        int rep = 0;
        int repline = 0;
        clearScreen();
        cursor(0,0);
        for (int phase = 0; phase < phases_nb; phase++){
            do {
                // Draw Enemy and Interface
                clearScreen();
                cursor(0,0);
                printTabColumn(enemy,0,"black"); // Draw Enemy
                if (phase > 0){
                    score += (int) (charAt(enemy[repline-1][3+(phase-1)*3],0)-'0'); // Update Score
                    printTextbox("Score : "+score,"purple",FIGHT_WIDTH); // Draw Score
                    printTextbox(enemy[repline-1][2+(phase-1)*3],"green",FIGHT_WIDTH); // Enemy Response
                } else {
                    printTextbox("Score : "+score,"purple",FIGHT_WIDTH); // Draw Score
                }
                printTextbox(enemy[0][1+phase*3],"green",FIGHT_WIDTH); // Enemy Dialog
                printTextbox(enemy[1][1+phase*3],"blue",FIGHT_WIDTH); // Answer List
                printTextbox("Tape 1, 2, 3, ou 4 puis Entrée pour valider.","black",FIGHT_WIDTH);
                printTextboxBorder(FIGHT_WIDTH);
                // Asks Answer
                text("black");
                rep = (int) (charAt(keyboardInput()+" ",0)-'0');
            } while (rep < 1 || rep > 4);
            repline = rep;
        }
        clearScreen();
        cursor(0,0);
        printTabColumn(enemy,0,"black"); // Draw Enemy
        score += (int) (charAt(enemy[repline-1][3+(phases_nb-1)*3],0)-'0'); // Update Score
        printTextbox("Score final : "+score,"purple",FIGHT_WIDTH); // Draw Score
        if (score >= score_min || equals(enemy_name,"enemy_south_0")){
            printTabColumn(enemy,10,"purple");
            win = true;
        } else {
            printTabColumn(enemy,11,"red");
            giveFragment(fragments, world_current, -1);
        }
        keyboardInput();
        background("black");
        return win;
    }

    void printTabColumn(String tab [][], int col, String color){
        int lig = 0;
        printTextboxBorder(FIGHT_WIDTH);
        while (!equals(tab[lig][col],"<end>") && lig < length(tab,1)){
            printTextbox(tab[lig][col],color,FIGHT_WIDTH);
            lig += 1;
        }
        printTextboxBorder(FIGHT_WIDTH);
    }

    void printTextboxBorder(int size){
        String res = "o";
        for (int i = 0; i < size-2; i++){
            res += "-";
        }
        text("white");
        println(res+"o");
    }

    void printTextbox(String str, String color, int size){
        text("white");
        print("|");
        text(color);
        print(stringCenter(str,size-2));
        text("white");
        println("|");
    }

    //------------------------//
    // INTERACTIONS FUNCTIONS //
    //------------------------//

    void menuNPC(String[][] npc_dialogs, Camera cam, String[][] world, Player player, String world_name, int[] fragments, String name){
        int dialog = 1;
        int npc_id = tabGetIndex(npc_dialogs[0],name);
        while (!equals(npc_dialogs[dialog][npc_id],"<end>")){
            drawWorld(cam,world,player,world_name);
            dialog(name,npc_dialogs[dialog][npc_id]);
            dialog += 1;
        }
    }

    int menuWizard(String[][] wizard_dialogs, Camera cam, String[][] world, String world_current, Player player, String world_name, int[] fragments, String name, int[] wizard_step, String last_wizard, String[][] questions, boolean last_answer_good){
        int answer = -1;
        int dialog = 1;
        int wizard_id = tabGetIndex(wizard_dialogs[0],name);
        String txt = wizard_dialogs[dialog][wizard_id];
        // Special Dialogs
        if (fragments[0]+fragments[1]+fragments[2] == 15){
            wizard_id = tabGetIndex(wizard_dialogs[0],"divinestone");
        } else if (fragments[0] == 5 && (currentVillage(world_current) == 0)
                || fragments[1] == 5 && (currentVillage(world_current) == 1)
                || fragments[2] == 5 && (currentVillage(world_current) == 2)){
            wizard_id = tabGetIndex(wizard_dialogs[0],"allfragments");
        } else if (wizard_step[wizard_id] == 4){
            wizard_id = tabGetIndex(wizard_dialogs[0],"nomorequestion");
        } else if (equals(name,last_wizard)){
            if (last_answer_good){
                wizard_id = tabGetIndex(wizard_dialogs[0],"comelater");
            } else {
                wizard_id = tabGetIndex(wizard_dialogs[0],"badanswer");
            }
        } else {
            while (!equals(txt,"<"+wizard_step[wizard_id]+">")){
                dialog += 1;
                txt = wizard_dialogs[dialog][wizard_id];
            }
        }
        txt = wizard_dialogs[dialog][wizard_id];
        // Ordinary Dialogs & Questions
        while (!equals(txt,"<end>")){
            drawWorld(cam,world,player,world_name);
            if (charAt(txt,0) == '<'){
                if (length(txt) > 9 && equals(substring(txt,1,9),"question")){
                    if (wizard_step[wizard_id] == 0 || wizard_step[wizard_id] == 2){
                        wizard_step[wizard_id] += 1;
                    }
                    if (askQuestion(questions[(int) (charAt(txt,9)-'0')*10 + (int) (charAt(txt,10)-'0')],name)){
                        wizard_step[wizard_id] += 1;
                        wizard_id = tabGetIndex(wizard_dialogs[0],"goodanswer");
                        giveFragment(fragments,world_current,1);
                        answer = 1;
                    } else {
                        wizard_id = tabGetIndex(wizard_dialogs[0],"badanswer");
                        answer = 0;
                    }
                    dialog = 0;
                }
            } else {
                dialog(name,txt);
            }
            dialog += 1;
            txt = wizard_dialogs[dialog][wizard_id];
        }
        return answer;
    }

    void dialog(String name, String txt){
        printTextboxBorder(64);
        printTextbox(name,"yellow",64);
        printTextbox("","white",64);
        printTextbox(txt,"white",64);
        printTextbox("","white",64);
        println("|                                                     Entrée > |");
        printTextboxBorder(64);
        keyboardInput();
    }

    boolean askQuestion(String[] question, String name){
        // Asks a question and returns true if the answer is correct
        printTextboxBorder(64);
        printTextbox(name,"yellow",64);
        printTextbox("","white",64);
        printTextbox(question[0],"green",64);
        printTextbox("","white",64);
        printTextbox(stringCenter("1. "+question[1],30)+stringCenter("2. "+question[2],30),"green",64);
        printTextbox(stringCenter("3. "+question[3],30)+stringCenter("4. "+question[4],30),"green",64);
        printTextbox("","white",64);
        printTextbox("Tape 1, 2, 3, ou 4 puis Entrée pour valider.","white",64);
        printTextboxBorder(64);
        String res;
        do {
            res = keyboardInput();
        } while (!equals(res,"1") && !equals(res,"2") && !equals(res,"3") && !equals(res,"4"));
        return equals(res,question[5]);
    }

    void giveFragment(int[] fragments, String world_current, int nb){
        // Gives nb fragments according to the village
        int village = currentVillage(world_current);
        if (village != 3){
            fragments[village] += nb;
            if (fragments[village] < 0){
                fragments[village] = 0;
            } else if (fragments[village] > 5){
                fragments[village] = 5;
            }
        }
    }

    int currentVillage(String world_current){
        int res = -1;
        if (equals(world_current,"village_west") || equals(substring(world_current,0,10),"house_west")){
            res = 0;
        } else if (equals(world_current,"village_south") || equals(substring(world_current,0,11),"house_south")){
            res = 1;
        } else if (equals(world_current,"village_east") || equals(substring(world_current,0,10),"house_east")){
            res = 2;
        } else if (equals(world_current,"village_north") || equals(world_current,"village_north_end")){
            res = 3;
        }
        return res;
    }

    boolean isMage(String npc_name){
        return equals(substring(npc_name,0,4),"Mage");
    }

    boolean canInteract(String[][] world, Player player){
    // Returns true if the player can interact with an NPC or something
        return (checkNextPlayerPosition(world,player) && charAt(world[player.y+player.dir[1]][player.x+player.dir[0]],4) == '4');
    }

    //-----------------//
    // FILES FUNCTIONS //
    //-----------------//

    String[][] csvLoad(String filename, int minStrLen){
        CSVFile csv = loadCSV(filename);
        String[][] res = new String[rowCount(csv)][columnCount(csv)];
        for (int lig = 0; lig < length(res,1); lig++){
            for (int col = 0; col < length(res,2); col++){
                res[lig][col] = stringAddSpaces(getCell(csv,lig,col),minStrLen);
            }
        }
        return res;
    }

    void saveGame(String file_nb, int[] worlds_x, int[] worlds_y, int[] wizard_step, int[] fragments, int[] enemies, String world_current, int game_step, Player player){
        String[][] savetemp = new String[6][length(worlds_x)];
        saveLine(savetemp,worlds_x,0);
        saveLine(savetemp,worlds_y,1);
        saveLine(savetemp,wizard_step,2);
        saveLine(savetemp,fragments,3);
        saveLine(savetemp,enemies,4);
        String[] savetemp2 = new String[]{world_current,game_step+"",player.x+"",player.y+"",player.spriteindex+"",player.dir[0]+"",player.dir[1]+""};
        for (int col = 0; col < 7; col++){
            savetemp[5][col] = savetemp2[col];
        }
        saveCSV(savetemp,"save"+file_nb+".csv");
    }

    void saveLine(String[][] save, int[] tab, int lig){
        for (int col = 0; col < length(tab); col++){
            save[lig][col] = tab[col]+"";
        }
    }

    String[] loadGame(String file_nb, int[] worlds_x, int[] worlds_y, int[] wizard_step, int[] fragments, int[] enemies, Player player){
        String[][] loadtemp = csvLoad("save"+file_nb+".csv",1);
        loadLine(loadtemp[0],worlds_x);
        loadLine(loadtemp[1],worlds_y);
        loadLine(loadtemp[2],wizard_step);
        loadLine(loadtemp[3],fragments);
        loadLine(loadtemp[4],enemies);
        player.x = stringToInt(loadtemp[5][2]);
        player.y = stringToInt(loadtemp[5][3]);
        player.spriteindex = stringToInt(loadtemp[5][4]);
        player.dir[0] = stringToInt(loadtemp[5][5]);
        player.dir[1] = stringToInt(loadtemp[5][6]);
        return new String[]{loadtemp[5][0],loadtemp[5][1]};
    }

    void loadLine(String[] saveLine, int[] tab){
        int col = 0;
        while (col < length(saveLine) && !equals(saveLine[col],"null")){
            tab[col] = stringToInt(saveLine[col]);
            col += 1;
        }
    }

    //------------------//
    // PLAYER FUNCTIONS //
    //------------------//

    Player newPlayer(int x, int y){
        Player player = new Player();
        player.x = x;
        player.y = y;
        player.spriteindex = 2;
        player.dir = new int[]{0,1};
        return player;
    }

    void movePlayer(Player player, int x, int y){
        player.x = x;
        player.y = y;
    }

    void keyboardMovePlayer(String key, String[][] world, Player player){
        if (equals(key,"Z")){ // Changes the X and Y direction
            player.dir[0] = 0;
            player.dir[1] = -1;
            player.spriteindex = 0;
        } else if (equals(key,"Q")){
            player.dir[0] = -1;
            player.dir[1] = 0;
            player.spriteindex = 1;
        } else if (equals(key,"S")){
            player.dir[0] = 0;
            player.dir[1] = 1;
            player.spriteindex = 2;
        } else if (equals(key,"D")){
            player.dir[0] = 1;
            player.dir[1] = 0;
            player.spriteindex = 3;
        }
        if (checkNextPlayerPosition(world,player) && !collision(world[player.y+player.dir[1]][player.x+player.dir[0]])){ // Actually moves the player
            player.x += player.dir[0];
            player.y += player.dir[1];
        }
    }

    void movePlayerToSpawn(String[][] world, Player player){
        int x = player.x;
        int y = player.y;
        boolean found = false;
        int lig = 0;
        while (lig < length(world,1) && !found){
            int col = 0;
            while (col < length(world,2)){
                if (charAt(world[lig][col],4) == '3'){
                    found = true;
                    player.x = col;
                    player.y = lig;
                }
                col += 1;
            }
            lig += 1;
        }
    }

    void playerSavePosition(String[] worlds_list, int[] worlds_x, int[] worlds_y, String world_current, Player player){
    // Saves the player position and returns the worlds number
        int idx = tabGetIndex(worlds_list,world_current);
        worlds_x[idx] = player.x;
        worlds_y[idx] = player.y;
    }

    void playerGotoSavedPosition(String[][] world, String[] worlds_list, int[] worlds_x, int[] worlds_y, String world_current, Player player){
    // Look for a saved player position and moves the player
        int idx = tabGetIndex(worlds_list,world_current);
        if (worlds_x[idx] == -1){
            movePlayerToSpawn(world,player); // By default, the player goes to the world spawn
        } else {
            movePlayer(player, worlds_x[idx], worlds_y[idx]); // The player moves to the saved position
        }
    }

    boolean checkNextPlayerPosition(String[][] world, Player player){
        // Returns true if the place where the player is about to go is within the bounds
        return (player.x + player.dir[0] >= 0 && player.x + player.dir[0] < length(world,2)
             && player.y + player.dir[1] >= 0 && player.y + player.dir[1] < length(world,1));
    }

    boolean collision(String place){
    // Returns true if there is a wall or an NPC
        return (charAt(place,4) == '1' || charAt(place,4) == '4');
    }

    //------------------//
    // CAMERA FUNCTIONS //
    //------------------//

    Camera newCamera(int x, int y){
        Camera cam = new Camera();
        cam.x = x;
        cam.y = y;
        cam.vw = 0;
        cam.vh = 0;
        return cam;
    }

    void camFollowPlayer(Camera cam, String[][] world, Player player){
        if (MAX_CAM_WIDTH > length(world,2)){ // Max View Width
            cam.vw = length(world,2);
        } else {
            cam.vw = MAX_CAM_WIDTH;
        }

        if (MAX_CAM_HEIGHT > length(world,1)){ // Max View Height
            cam.vh = length(world,1);
        } else {
            cam.vh = MAX_CAM_HEIGHT;
        }

        cam.x = player.x-cam.vw/2; // X position
        if (cam.x < 0){
            cam.x = 0;
        } else if (cam.x > length(world,2)-cam.vw){
            cam.x = length(world,2)-cam.vw;
        }

        cam.y = player.y-cam.vh/2; // Y position
        if (cam.y < 0){
            cam.y = 0;
        } else if (cam.y > length(world,1)-cam.vh){
            cam.y = length(world,1)-cam.vh;
        }
    }

    //-----------------//
    // OTHER FUNCTIONS //
    //-----------------//
    
    
    String keyboardInput(){
    // Returns the user string input in capital
        String res;
        res = stringUpper(readString());
        return res;
    }

    String stringUpper(String str){
    // Returns the input string in capital
        String res = "";
        for (int i = 0; i < length(str); i++){
            char c = charAt(str,i);
            if (c >= 'a' && c <= 'z'){
                res = res + (char) (c-32);
            } else {
                res = res + c;
            }
        }
        return res;
    }

    String stringAddSpaces(String str, int len){
    // Adds enough spaces to get the input string to the required length and return it
        String res = "";
        int maxlen = len;
        if (length(str) > maxlen){
            maxlen = length(str);
        }
        for (int i = 0; i < maxlen; i++){
            if (i >= length(str)){
                res = res + " ";
            } else {
                res = res + charAt(str,i);
            }
        }
        return res;
    }

    String stringCenter(String str, int len){
    // Returns the input string centered in the middle of the given length
        String res = "";
        for (int i = 0; i < len/2-length(str)/2; i++){
            res = res + " ";
        }
        res = res + str;
        while (length(res) < len){
            res = res + " ";
        }
        return res;
    }

    int tabGetIndex(String[] tab, String element){
    // Returns the index of an element in a string table
        int res = -1;
        int i = 0;
        while (i < length(tab) && res == -1){
            if (equals(tab[i],element)){
                res = i;
            }
            i += 1;
        }
        return res;
    }

    int tabGetIndex(int[] tab, int element){
    // Returns the index of an element in an integer table
        int res = -1;
        int i = 0;
        while (i < length(tab) && res == -1){
            if (tab[i] == element){
                res = i;
            }
            i += 1;
        }
        return res;
    }

    int[] generateIntTable(int size, int value){
    // Generates an integer table full of a given value
        int[] res = new int[size];
        for (int i = 0; i < size; i++){
            res[i] = value;
        }
        return res;
    }

    //----------------//
    // TEST FUNCTIONS //
    //----------------//

    void testGenerateIntTable(){
        int[] tab = new int[]{-1,-1,-1};
        assertArrayEquals(tab,generateIntTable(3,-1));
    }

    void testTabGetIndex(){
        int[] tab1 = new int[]{6,4,3};
        assertEquals(1,tabGetIndex(tab1,4));
        String[] tab2 = new String[]{"a","b","c","d"};
        assertEquals(2,tabGetIndex(tab2,"c"));
    }

    void testStringAddSpaces(){
        assertEquals("a  ",stringAddSpaces("a",3));
        assertEquals("Arbre",stringAddSpaces("Arbre",3));
        assertEquals("maison     ",stringAddSpaces("maison",11));
    }

    void testStringCenter(){
        assertEquals(" a ",stringCenter("a",3));
        assertEquals("Arbre",stringCenter("Arbre",3));
        assertEquals("  maison   ",stringCenter("maison",11));
    }

    void testStringUpper(){
        assertEquals("MAISON",stringUpper("maison"));
        assertEquals("ARBRE",stringUpper("ARBRE"));
        assertEquals("JàRDIN-",stringUpper("jàrdIn-"));
    }

    void testCollision(){
        assertTrue(collision("12__1"));
        assertFalse(collision("03   "));
        assertFalse(collision("55||0"));
        assertTrue(collision("1___1"));
        assertFalse(collision("11ab2"));
    }

    void testIsMage(){
        assertTrue(isMage("Mage Heure"));
        assertFalse(isMage("Floraine"));
        assertTrue(isMage("Mage Ustice"));
        assertFalse(isMage("MAge Ongue"));
        assertFalse(isMage("mage uscule"));
    }

    void testCurrentVillage(){
        assertEquals(1,currentVillage("village_south"));
        assertEquals(2,currentVillage("house_east_0"));
        assertEquals(0,currentVillage("village_west"));
        assertEquals(-1,currentVillage("duniya_plain"));
        assertEquals(3,currentVillage("village_north"));
    }

    void testGiveFragment(){
        int[] frag = new int[]{1,2,3};
        giveFragment(frag,"village_south",1);
        assertArrayEquals(frag,new int[]{1,3,3});
        giveFragment(frag,"village_east",-1);
        assertArrayEquals(frag,new int[]{1,3,2});
        giveFragment(frag,"village_west",-2);
        assertArrayEquals(frag,new int[]{0,3,2});
    }
}