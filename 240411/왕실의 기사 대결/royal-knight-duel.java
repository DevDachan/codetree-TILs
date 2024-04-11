import java.util.*;
import java.io.*;

/*

L x L 크기의 체스판 위에서의 대결

함정 : 1
빈칸 : 0
벽   : 2

기사는 마력으로 상대를 밀쳐낸다.
-> 각 기사는 r,c에 위치하며 h x w크기의 직사각형을 띄고 있다. 
   (r,c)가 좌측 상단

각 기사의 체력은 k로 주어짐


1. 기사 이동
4방탐색, 한칸 이동

-> 다른 기사가 있다면 연쇄적으로 한칸 밀어내기
-> 끝에 벽이 있다면 모든 기사는 이동할 수 없다.

체스판에서 사라진 기사에게 명령을 내릴 경우에는 아무런 반응이 없음
-> HashMap


2. 대결 대미지
명령으로 기사가 밀려나 다른 기사를 밀치면 밀려난 기사들은 피해를 입는다.

-> 각 기사들은 해당 기사가 이동한 곳에서 w x h 직사각형 내에 놓인 함정수만큼 피해를 입음
-> 현재 체력 이상의 대미지를 받을 경우 소멸된다.

단!! 명령을 받은 기사는 피해를 입지 않음
-> 밀렸더라도 밀쳐진 위치에 함정이 없다면 피해를 입지 않는다.
-> 대미지는 모든 기사가 밀린 이후에 받는다


Q번에 걸쳐 왕의 명령이 주어질때 기사들이 받은 총 대미지의 합을 출력해라


풀이

HashMap<Integer, List<Position>> knights

HashMap<Integer, Integer> hp

int[][] map, kightsMap

-> 각 기사들을 확인할 수 있는 맵



*/
public class Main {

    public static class Position{
        int x,y;
        Position(int x, int y){
            this.x = x;
            this.y = y;
        }
    }

    public static Position[] deltas = {
        new Position(-1,0),new Position(0,1),
        new Position(1,0),new Position(0,-1), 
    };

    public static final int EMPTY = 0, TRAP = 1, WALL = 2;

    public static int N, L, Q;

    public static int[][] map, knightMap;

    public static HashMap<Integer, Integer> hp = new HashMap<>();

    public static HashMap<Integer, List<Position>> knights = new HashMap<>();

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        L = Integer.parseInt(st.nextToken());
        N = Integer.parseInt(st.nextToken());
        Q = Integer.parseInt(st.nextToken());

        knightMap = new int[L][L];

        // map 입력 (함정은 번호를 음수로 매긴다)
        map = new int[L][L];
        int trapNum = -1;
        for(int i = 0; i < L; i++){
            st = new StringTokenizer(br.readLine());
            for(int j = 0; j < L; j++){
                map[i][j] = Integer.parseInt(st.nextToken());
                if(map[i][j] == TRAP){
                    map[i][j] = trapNum--;
                }
            }
        }
        
        // 초기 기사 입력
        Queue<Integer> alive = new ArrayDeque<>();
        int r,c,h,w,k;
        for(int num = 1; num <= N; num++){
            st = new StringTokenizer(br.readLine());
            // one base to zero base
            r = Integer.parseInt(st.nextToken())-1;
            c = Integer.parseInt(st.nextToken())-1;
            h = Integer.parseInt(st.nextToken());
            w = Integer.parseInt(st.nextToken());
            k = Integer.parseInt(st.nextToken());
            List<Position> temp = new ArrayList<>();

            for(int x = r; x < r+h; x++){
                for(int y = c; y < c+w; y++){
                    knightMap[x][y] = num;
                    temp.add(new Position(x,y));
                }
            }

            // 각 기사마다의 위치 저장
            knights.put(num, temp);

            // 기사의 hp 저장
            hp.put(num, k);
        } 

        int result = 0;
        Queue<Integer> qu = new ArrayDeque<>();
        HashSet<Integer> visited = new HashSet<>();
        HashSet<Integer> damageCount = new HashSet<>();
        int nextX,nextY, damage;
        boolean nextCheck;
        
        for(int step = 0; step < Q; step++){
            st = new StringTokenizer(br.readLine());
            int num = Integer.parseInt(st.nextToken());
            int dir = Integer.parseInt(st.nextToken());

            // 현재 기사가 살아 있을 경우에만 확인하기
            if(hp.containsKey(num)){

                // 해당 기사부터 한번 밀수 있는지 확인하기
                // 다른 기사를 만나면 추가, 밀수 없다면 false
                qu.clear();
                visited.clear();
                qu.add(num);
                visited.add(num);
                nextCheck = false;

                while(!qu.isEmpty()){
                    int cur = qu.poll();    
                    for(Position i : knights.get(cur)){
                        nextX = i.x + deltas[dir].x;
                        nextY = i.y + deltas[dir].y;

                        // 이동 가능하다면 확인하기
                        if(check(nextX, nextY)){
                            if(knightMap[nextX][nextY] > 0 && !visited.contains(knightMap[nextX][nextY])){
                                visited.add(knightMap[nextX][nextY]);
                                qu.add(knightMap[nextX][nextY]);
                            }
                        }else{
                            nextCheck = true;
                            break;
                        }
                    }
                }   

                // 만약 미는데 문제가 없다면 밀기 시작!
                if(!nextCheck){
                    
                    // 방문했던 모든 기사 밀기
                    for(int cur : visited){

                        damageCount.clear();
                        // 모든 몸체 미는데 대미지 확인
                        // 이동한 곳에서 w x h 내에 놓인 함정의 수만큼 피해
                        for(Position i : knights.get(cur)){
                            nextX = i.x + deltas[dir].x;
                            nextY = i.y + deltas[dir].y;
                            knightMap[nextX][nextY] = cur;
                            if(map[nextX][nextY] < 0){
                                damageCount.add(map[nextX][nextY]);
                            }
                        }

                        // 현재 기사는 피를 깎지 않는다.
                        if(cur == num) continue;

                        // hp에서 깎고 결과 값에 더하기
                        hp.put(cur, hp.get(cur)- damageCount.size());
                        result += damageCount.size();

                        // 만약 피가 전부 달았다면 죽이기
                        if(hp.get(cur) < 0){
                            hp.remove(cur);
                            for(Position i : knights.get(cur)){
                                knightMap[i.x][i.y] = 0;
                            }
                            knights.remove(cur);
                        }
                    }

                }
            }
        }


        System.out.println(result);
    }

    public static boolean check(int x, int y){
        return x >= 0 && y >= 0 && x < L && y < L && map[x][y] != WALL;
    }
}