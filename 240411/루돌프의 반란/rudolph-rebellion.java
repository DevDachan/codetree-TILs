import java.util.*;
import java.io.*;

/*

N X N 격자로 이루어져 있다.
-> (1,1) 1 base index

게임은 M개의 턴으로 진행
-> 루돌프와 산타는 한번씩 움직인다.


1. 게임 룰
루돌프가 한번 움직인뒤 1번~P번 산타는 순서대로 움직인다.
-> 이때 기절했거나 탈락한 산타는 가만히 있는다.

--> int len(int r, int c)
각 칸간 거리는 (r-r)^2 + (c-c)^2로 계산된다.


2. 루돌프
루돌프는 가장 가까운 산타에게 1칸 돌진
-> 탈락한 산타는 제외한다.

만약 가장 가까운 산타가2명 이상이라면 r좌표가 큰 산타에게 돌진한다.
-> 그것도 아니라면 c좌표가 큰 산타에게 돌진한다.

--> compare(Position o) if(o.x == this.x ) return this.x < o.x

루돌프는 8방 탐색을 한다.


3. 산타
산타는 1번부터 P번까지 순서대로 움직인다
-> 기절했거나 탈락한 산타는 움직이지 않는다
-> 산타는 루돌프에게 거리가 가까워 지는 방향으로 1칸 이동한다
-> 다른 산타가 있거나 게임판 밖으로는 이동하지 않는다
-> 움직일 수 있는 칸이 없다면 움직이지 않는다

움직일 수 있는 칸이 있더라도 루돌프에게 가까워지지 않는다면 움직이지 않는다

산타는 4방탐색을 한다
-> 만약 움직일 수 있는 방향이 여러개라면 상우하좌 순서로 움직인다.

4방 탐색, 상우하좌, 가까워 지는 방향



4. 충돌
루돌프와 산타가 같은 칸에 있다면 충돌

(4-1) 루돌프가 움직여 충돌을 했다면 산타는 C만큼 점수를 얻는다
-> 이와 동시에 산타는 루돌프가 이동해온 방향으로 C칸 만큼 밀려난다

(4-2) 산타가 움직여 충돌할 경우 산타는 D만큼의 점수를 얻는다
-> 이와 동시에 자신이 이동해온 반대 방향으로 D만큼 밀려난다

4-1과 4-2의 방향이 다름을 주의해라
-> 밀려나면서는 충돌이 일어나지 않는다
-> 밀려난 칸이 밖이라면 탈락한다
-> 다른 산타가 있다면 상호작용이 발생한다


5. 상호작용
-> 상호작용은 4의 방법으로만 가능하다

다른 산타가 먼저 위치해 있다면 해당 산타는 1칸 해당 방향으로 밀려난다
-> 그옆에 다른 산타가 있다면 동일하게 반복 된다.
-> 게임판 밖으로 밀려난 산타는 탈락한다.


6. 기절
산타는 루돌프와 충돌하면 기절한다
-> k번째 턴에 기절했다면 k+1턴은 가만히 있고 k+1부터 정상이 된다.

기절한 상태에서 도중 충돌이나 상호작용은 가능


7. 게임 종료

-> M번의 턴을 모두 끝내면 게임 종료
-> 산타 P명이 모두 탈락하면 종료
-> 매턴 이후 탈락하지 않은 산타는 1점을 추가로 부여한다.

각 산타가 얻은 최종 점수를 구해야 한다.



풀이
먼저 N X N의 맵을 그린다
-1은 루돌프의 위치, 0보다 클 경우 산타, 0은 빈칸

-> 산타를 관리할 객체를 생성한다
Queue로 하면 순서가 좋지만 계속 뺐다 꼈다하면 불편함
-> 산타는 최대 30, 1000턴이므로 List를 활용
-> 죽은 산타는 remove하기

int[] score
-> 산타의 전체 스코어 판을 만들어서 탈락하는 동시에 점수를 기록하기


*/
public class Main {

    public static class Position{
        int x, y;
        Position(int x, int y){
            this.x = x;
            this.y = y;
        }

        // 두 점 사이의 거리
        public double len(int x, int y){
            return Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2);
        }

        // 입력 좌표가 해당 좌표보다 우선순위가 큰지 확인
        public boolean compare(int x, int y){
            if(this.x == x) return this.y < y;
            return this.x < x;
        }
    }
    
    public static class Santa extends Position{
        int score, stun, num;
        Santa(int x, int y, int num,int score, int stun){
            super(x,y);
            this.num = num;
            this.score = score;
            this.stun = stun;
        }

        public boolean isStun(int turn){
            return stun == turn-1 || stun == turn;
        }
    }
    

    // 루돌프는 8방 탐색
    public static Position[] deltas = { 
            new Position(-1,-1), new Position(-1,0), new Position(-1,1), 
            new Position(0,-1),                      new Position(0,1), 
            new Position(1,-1), new Position(1,0), new Position(1,1),
    };   

    // 산타는 4방 탐색, 상우하좌
    public static Position[] deltasSanta = { 
            new Position(-1,0), 
            new Position(0,1), new Position(1,0), 
            new Position(0,-1),
    };            
             
    
    public static int[][] map;

    public static int[] score;

    public static List<Santa> santas;

    public static int n, turns, santaNum, dolphPower, santaPower;

    public static Position dolph;

    public static void main(String[] args) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());    
        n = Integer.parseInt(st.nextToken());
        turns = Integer.parseInt(st.nextToken());
        santaNum = Integer.parseInt(st.nextToken());
        dolphPower = Integer.parseInt(st.nextToken());
        santaPower = Integer.parseInt(st.nextToken());
        
        santas = new ArrayList<>(santaNum+1);
        for (int i = 0; i <= santaNum; i++) {
            santas.add(null);
        }

        map = new int[n+1][n+1];
        score = new int[santaNum+1];

        st = new StringTokenizer(br.readLine());
        dolph = new Position(Integer.parseInt(st.nextToken()),Integer.parseInt(st.nextToken()) );
        map[dolph.x][dolph.y] = -1;
        
        for(int i = 0; i < santaNum; i++){
            st = new StringTokenizer(br.readLine());
            int num = Integer.parseInt(st.nextToken());
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            santas.set(num,new Santa(x,y,num,0,-2));
            map[x][y] = num;
        }

        for(int turn = 1; turn <= turns; turn++){

            // 먼저 루돌프가 움직인다.
            goDolph(turn);
            
            // 이후 산타가 움직인다
            goSanta(turn);

            if(updateSanta() == 0) break;
        }
        
        aliveSanta();

        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= santaNum; i++){
            sb.append(score[i]).append(" ");
        }
        
        System.out.println(sb);
    }

    public static void aliveSanta(){
        for(int i = 1; i <= santaNum; i++){
            if(santas.get(i) != null){
                score[i] = santas.get(i).score;
            }
        }
    }

    public static int updateSanta(){
        int alive = 0;
        for(int i = 1; i <= santaNum; i++){
            if(santas.get(i) != null){
                alive++;
                santas.get(i).score++;
            }
        }
        return alive;
    }

    public static void goDolph(int turn){
        double minLen = Double.MAX_VALUE;
        int minNum = 0;

        // 각 산타를 확인하며 가장 가깝고 우선순위가 큰 산타를 찾기
        for(Santa i : santas){
            // 8방 탐색으로 이동이 가능한지 체크한다.
            if(i != null){
                // 현재 돌프와의 거리를 확인한다.
                double len = i.len(dolph.x, dolph.y);
                if(len < minLen){
                    minNum = i.num;
                    minLen = len;
                }else if(len == minLen && !i.compare(santas.get(minNum).x, santas.get(minNum).y)){
                    minNum = i.num;
                }
            }
        }
        
        // 산타에게 다가가기
        int dir = dirCheck(santas.get(minNum));
        int nextX = dolph.x + deltas[dir].x;
        int nextY = dolph.y + deltas[dir].y;
        
        // 만약 부딪혔다면?
        if(map[nextX][nextY] > 0){
            santas.get(minNum).score += dolphPower;

            // 산타가 밀리며 연쇄작용
            Queue<Santa> qu = new ArrayDeque<>();
            Santa cur = santas.get(minNum);
            nextX += deltas[dir].x* dolphPower;
            nextY += deltas[dir].y* dolphPower;

            // 경기장 밖이라면 아웃
            if(!check(nextX, nextY)){
                score[cur.num] = cur.score;
                map[cur.x][cur.y] = 0;
                santas.set(cur.num, null);
            }else{
                if(map[nextX][nextY] > 0){
                    qu.add(santas.get(map[nextX][nextY]));
                }
                map[nextX][nextY] = cur.num;
                cur.x = nextX;
                cur.y = nextY;
                cur.stun = turn;

                while(!qu.isEmpty()){
                    cur = qu.poll();
                    nextX = cur.x + deltas[dir].x;
                    nextY = cur.y + deltas[dir].y;
                    if(check(nextX, nextY)){
                        if(map[nextX][nextY] > 0){
                            qu.add(santas.get(map[nextX][nextY]));
                        }
                        map[nextX][nextY] = cur.num;
                        cur.x = nextX;
                        cur.y = nextY;
                    }else{
                        score[cur.num] = cur.score;
                        santas.set(cur.num, null);
                    }
                }
            }
        }
        // 루돌프 전진
        map[dolph.x][dolph.y] = 0;
        dolph.x += deltas[dir].x;
        dolph.y += deltas[dir].y;
        map[dolph.x][dolph.y] = -1;
    }

    public static void goSanta(int turn){
        Santa cur;
        for(int i = 1; i <= santaNum; i++){
            cur = santas.get(i);
            if(cur != null && !cur.isStun(turn)){
                double minLen = dolph.len(cur.x, cur.y);
                int minDir = -1;

                // 가장 거리가 가까운 방향 찾기
                for(int d = 0; d < 4; d++){
                    Position delta = deltasSanta[d];
                    int nextX = cur.x + delta.x;
                    int nextY = cur.y + delta.y;
                    
                    if(check(nextX, nextY) && map[nextX][nextY] <= 0){
                        double len = dolph.len(nextX, nextY);
                        if(len < minLen){
                            minDir = d;
                            minLen = len;
                        }
                    }
                }

                if(minDir == -1) continue;

                // 해당 방향으로 전진하기  
                int nextX = cur.x + deltasSanta[minDir].x;
                int nextY = cur.y + deltasSanta[minDir].y;

                // 루돌프와 충돌
                if(map[nextX][nextY] == -1){

                    // 점수 획득
                    cur.score += santaPower;

                    // 산타가 밀리며 연쇄작용
                    Queue<Santa> qu = new ArrayDeque<>();
                    minDir = inverseDir(minDir);
                    nextX += deltasSanta[minDir].x * santaPower;
                    nextY += deltasSanta[minDir].y * santaPower;
				    map[cur.x][cur.y] = 0;
                
                    // 경기장 밖이라면 아웃
                    if(!check(nextX, nextY)){
                        score[cur.num] = cur.score;
                        santas.set(cur.num,null);
                    }else{
                        // 이동한 위치에 다른 산타가 있다면 연쇄 시작
                        if(map[nextX][nextY] > 0){
                            qu.add(santas.get(map[nextX][nextY]));
                        }
                        map[cur.x][cur.y] = 0;
                        map[nextX][nextY] = cur.num;
                        cur.x = nextX;
                        cur.y = nextY;
                        cur.stun = turn;
                        
                        while(!qu.isEmpty()){
                            cur = qu.poll();
                            nextX = cur.x + deltasSanta[minDir].x;
                            nextY = cur.y + deltasSanta[minDir].y;
                            if(check(nextX, nextY)){
                                if(map[nextX][nextY] > 0){
                                    qu.add(santas.get(map[nextX][nextY]));
                                }
                                map[nextX][nextY] = cur.num;
                                cur.x = nextX;
                                cur.y = nextY;
                            }else{
                                score[cur.num] = cur.score;
                                santas.set(cur.num, null);
                            }
                        }
                    }
                }else{
                    map[cur.x][cur.y] = 0;
                    cur.x += deltasSanta[minDir].x;
                    cur.y += deltasSanta[minDir].y;
                    map[cur.x][cur.y] = cur.num;
                }
            }
        }

    }

    // 상우하좌 -> 하좌상우
    public static int inverseDir(int dir){
        if(dir == 0){
            return 2;
        }else if(dir == 2){
            return 0;
        }else if(dir == 1){
            return 3;
        }else{
            return 1;
        }
    }

    public static int dirSantaCheck(Santa i){
        if(Math.abs(dolph.x - i.x) < Math.abs(dolph.y - i.y)){
            // 오른쪽으로 이동
            if(dolph.y < i.y){
                return 3;
            // 왼쪽으로 이동
            }else{
                return 4;
            }
        }else{
            // 위로 이동
            if(dolph.x < i.x){
                return 1;
            // 아래로 이동
            }else{
                return 6;
            }
        }
    }   

    public static int dirCheck(Santa i){
        if(dolph.x == i.x){
            // 왼쪽으로 이동
            if(dolph.y < i.y){
                return 4;
            // 오른쪽으로 이동
            }else{
                return 3;
            }
        }else if(dolph.y == i.y){
            // 아래로 이동
            if(dolph.x < i.x){
                return 6;
            // 위로 이동
            }else{
                return 1;
            }
        }else{
            if(dolph.x < i.x && dolph.y < i.y){
                // 우하단으로 이동
                return 7;
            }else if(dolph.x < i.x && dolph.y > i.y){
                // 좌하단으로 이동    
                return 5;
            }else if(dolph.x > i.x && dolph.y < i.y){
                // 좌하단으로 이동    
                return 2;
            }else{
                // 우하단으로 이동   
                return 0;
            }
        }
    }

    public static boolean stunCheck(Santa i){
        // 대각선, 4방 확인
        return dolph.x-i.x == 0 || dolph.y-i.y == 0 || Math.abs(dolph.x - i.x) == Math.abs(dolph.y - i.y);
    }

    public static boolean check(int x, int y){
        return x > 0 && y > 0 && x <= n && y <= n;
    }
}