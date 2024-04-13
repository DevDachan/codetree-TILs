import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

public class Main {

	public static class Position {
		int x, y;

		Position(int x, int y) {
			this.x = x;
			this.y = y;
		}

		int len(int x, int y) {
			return Math.abs(this.x - x) + Math.abs(this.y - y);
		}

		boolean equals(int x, int y) {
			return this.x == x && this.y == y;
		}
	}

	public static class People extends Position {
		// 참가자 번호와 이동 거리
		int num, len;
		// 탈출 여부
		boolean state;

		People(int x, int y, int num, int len, boolean state) {
			super(x, y);
			this.num = num;
			this.len = len;
			this.state = state;
		}
	}

	public static Position[] deltas = { new Position(-1, 0), new Position(1, 0), new Position(0, 1),
			new Position(0, -1), };

	public static Position exit;

	public static int n, m, k, result, alivePeople;

	// EXIT = -1 , 벽 = 양수, 빈칸 : 0
	public static int[][] map, copyMap;

	public static final int EXIT = -1, EMPTY = 0;

	public static HashMap<Integer, People> peoples = new HashMap<>();

	public static HashSet<Integer>[][] peopleMap, copyPeopleMap;

	public static int boxPeople, boxLen;

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());

		n = Integer.parseInt(st.nextToken());
		m = Integer.parseInt(st.nextToken());
		k = Integer.parseInt(st.nextToken());

		// 벽과 exit을 저장
		map = new int[n][n];
		copyMap = new int[n][n];

		// 각 맵에 퍼져있는 참가자 번호
		peopleMap = new HashSet[n][n];
		copyPeopleMap = new HashSet[n][n];

		for (int i = 0; i < n; i++) {
			st = new StringTokenizer(br.readLine());
			for (int j = 0; j < n; j++) {
				peopleMap[i][j] = new HashSet<>();
				map[i][j] = Integer.parseInt(st.nextToken());
			}
		}

		int peopleNum = 0;
		for (int i = 0; i < m; i++) {
			peopleNum++;
			st = new StringTokenizer(br.readLine());
			int x = Integer.parseInt(st.nextToken()) - 1;
			int y = Integer.parseInt(st.nextToken()) - 1;
			peoples.put(peopleNum, new People(x, y, peopleNum, 0, true));
			peopleMap[x][y].add(peopleNum);
		}
		alivePeople = peopleNum;
		st = new StringTokenizer(br.readLine());
		exit = new Position(Integer.parseInt(st.nextToken()) - 1, Integer.parseInt(st.nextToken()) - 1);
		map[exit.x][exit.y] = EXIT;

		// k까지의 스테이지를 클리어한다.
		for (int stage = 0; stage < k; stage++) {
			// 살아남은 사람이 없다면 종료
			if (alivePeople <= 0)
				break;

			boxPeople = 0;
			boxLen = Integer.MAX_VALUE;
			// 1. 모든 참가자가 출구 방향으로 이동한다.
			movePeople();

			if (boxPeople == 0)
				break;
			// 2. 회전시킨다
			rotate();

		}

		for (int i : peoples.keySet()) {
			result += peoples.get(i).len;
		}
		System.out.println(result);
		System.out.println((exit.x + 1) + " " + (exit.y + 1));
	}

	public static void rotate() {
		// 최소 거리는 가장 가까운 참가자로부터 구한다.
		int startX = 0, startY = 0;
		int len = 1;
		mainout: for (int sz = 1; sz <= 10; sz++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {

					if (i + sz >= n || j + sz >= n)
						continue;

					// 만약 exit이 포함되어 있지 않다면 pass
					if (!(i <= exit.x && i + sz >= exit.x) || !(j <= exit.y && j + sz >= exit.y))
						continue;

					// 해당 범위 안에 다른 참가자가 존재하는지 확인합니다
					boolean contain = false;
					xout: for (int x = i; x <= i + sz; x++) {
						for (int y = j; y <= j + sz; y++) {
							if (peopleMap[x][y].size() > 0) {
								contain = true;
								break xout;
							}
						}
					}
					// 포함하고 있다면 여기부터 시작하기
					if (contain) {
						startX = i;
						startY = j;
						len = sz;
						break mainout;
					}

				}
			}
		}

		// 회전시키기
		for (int i = 0; i <= len; i++) {
			for (int j = 0; j <= len; j++) {
				copyPeopleMap[i][j] = peopleMap[startX + i][startY + j];
				copyMap[i][j] = map[startX + i][startY + j];
			}
		}

		for (int i = 0; i <= len; i++) {
			for (int j = 0; j <= len; j++) {
				peopleMap[startX + i][startY + j] = copyPeopleMap[len - j][i];
				map[startX + i][startY + j] = copyMap[len - j][i];
				if (map[startX + i][startY + j] > 0) {
					map[startX + i][startY + j]--;
				} else if (map[startX + i][startY + j] == EXIT) {
					exit.x = startX + i;
					exit.y = startY + j;
				}
				if (peopleMap[startX + i][startY + j].size() > 0) {
					// 바뀐 참가자 모두 위치 변경해주기
					for (int num : peopleMap[startX + i][startY + j]) {
						peoples.get(num).x = startX + i;
						peoples.get(num).y = startY + j;
					}
				}
			}
		}

	}

	public static void movePeople() {
		int nextX, nextY;
		out: for (int i = 1; i <= peoples.size(); i++) {
			People cur = peoples.get(i);

			// 살아 있을 경우에만 움직이기
			if (cur.state) {
				int minLen = exit.len(cur.x, cur.y);
				Position minPosition = new Position(-1, -1);

				for (Position delta : deltas) {
					nextX = cur.x + delta.x;
					nextY = cur.y + delta.y;
					if (check(nextX, nextY)) {
						// 탈출
						int curLen = exit.len(nextX, nextY);
						if (exit.equals(nextX, nextY)) {
							cur.len++;
							cur.state = false;
							alivePeople--;
							peopleMap[cur.x][cur.y].remove(cur.num);
							continue out;

							// deltas의 순서가 상부터 확인하므로 작을때만 업데이트 하면 됨
						} else if (map[nextX][nextY] == EMPTY && curLen < minLen) {
							minPosition.x = nextX;
							minPosition.y = nextY;
							minLen = curLen;
						}
					}
				}

				if (minPosition.x != -1) {
					// 최단거리를 찾았다면 이동하기
					peopleMap[cur.x][cur.y].remove(cur.num);
					cur.x = minPosition.x;
					cur.y = minPosition.y;
					peopleMap[cur.x][cur.y].add(cur.num);
					cur.len++;
				}

				// 이동 이후 exit와 가장 가까운 사용자 찾기
				if (boxLen > exit.len(cur.x, cur.y)) {
					boxPeople = cur.num;
					boxLen = exit.len(cur.x, cur.y);
				}
			}
		}
	}

	public static boolean check(int x, int y) {
		return x >= 0 && y >= 0 && x < n && y < n && map[x][y] <= 0;
	}
}