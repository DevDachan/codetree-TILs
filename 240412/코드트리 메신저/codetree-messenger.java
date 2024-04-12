import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.StringTokenizer;

/*

2진 트리 형태

-> 입력은 배열 형태로 주어짐
세그 트리?

----------------------------------------------------

1. 사내 메신저 준비

0~N번까지 N+1개의 채팅방 존재 (0 base index이지만 N까지)
-> 메인 채팅방은 항상 0번

입력으로 주어지는 것은 각각의 parent를 나타낸다
-> 0은 부모가 없으니 나타나지 않음 (순서대로 들어오는 것은 아니다)

authority: 각 채팅방의 권한
-> c번 방에서 메세지를 보내면 그 채팅방의 상위 a만큼만 올라간다.

0번은 parent, auth 모두 관련 없다.


2. 알림망 설정 ON / OFF
** 초기값은 true

해당 기능이 설정되면 c번의 상태를 토글한다.
-> 만약 OFF 상태 일 경우 위 아래로는 알림을 보내지 않는다.


3. 권한 세기 변경
c번의 세기를 변경한다.


4. 부모 채팅방 교환
-> c1번과 c2의 부모를 서로 바꾼다.
-> 이때 c1번 채팅방과 c2번 채팅방은 같은 depth상에 있음 무조건

그냥 두개의 값을 변경하면 될듯?


5. 알림 받을 수 있는 채팅방 조회
-> 메세지를 보낼때 c번 채팅방까지 알림이 도달 할 수 있는 서로다른 채팅방의 수를 출력한다.
set을 두고 모든 노드 확인?? (N == 100_000) 많을지도..? -> 자기부터 아래만 확인하면 됨
-> 순서를 확인할 때는 Node의 depth별로 확인하며 OFF된 곳은 pass하기


Q번에 걸친 명령을 순서대로 진행해 알맞은 답을 출력해라


// 주의사항!!
 * 알림 채팅방 조회시 자신은 제외 한다.

1. setting : 100 p1 p2 ... pN a1 a2 .... aN (무조건 처음 한번만 나옴)

2. toggle: 200 c

3. change: 300 c power

4. search: 500 c



 */

public class Main {

	public static class Node {
		int num, auth;
		boolean offset;
		Node parent, left, right;

		Node(int num, int auth, boolean offset, Node parent, Node left, Node right) {
			this.num = num;
			this.left = left;
			this.right = right;
			this.parent = parent;
			this.auth = auth;
			this.offset = offset;
		}
	}

	public static Node[] tree;

	public static int Q, N, D;

	public static final int PARENTS = 0, AUTH = 1, OFFSET = 2, OFF = 1, ON = 0;

	public static final int MY = 1;

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		StringBuilder sb = new StringBuilder();
		N = Integer.parseInt(st.nextToken());
		Q = Integer.parseInt(st.nextToken());

		// 복사용 임시 배열 선언
		tree = new Node[N + 1];

		// 1번째는 반드시 100이 나옴
		tree[0] = new Node(0, 0, true, null, null, null);

		st = new StringTokenizer(br.readLine());
		st.nextToken();
		for (int i = 1; i <= N; i++) {
			int p = Integer.parseInt(st.nextToken());
			tree[i] = new Node(i, 0, true, tree[p], null, null);
			if (tree[p].left == null)
				tree[p].left = tree[i];
			else
				tree[p].right = tree[i];
		}

		for (int i = 1; i <= N; i++) {
			tree[i].auth = Integer.parseInt(st.nextToken());
		}

		int type, c, c2, power;
		for (int step = 1; step < Q; step++) {
			st = new StringTokenizer(br.readLine());
			type = Integer.parseInt(st.nextToken());
			c = Integer.parseInt(st.nextToken());

			if (type == 200) {
				// 알림방 토글
				tree[c].offset = !tree[c].offset;
			} else if (type == 300) {
				// 권한 세기 변경 power로
				power = Integer.parseInt(st.nextToken());

				tree[c].auth = power;
			} else if (type == 400) {
				// 부모 교환
				c2 = Integer.parseInt(st.nextToken());
				swap(c, c2);

			} else {
				// 채팅 방 수 조회
				sb.append(count(c)).append("\n");
			}
		}
		System.out.println(sb);
	}

	public static int count(int c) {
		Node start = tree[c];
		int result = 0;
		int len = 0;
		Queue<Node> qu = new ArrayDeque<>();
		if (start.left != null && start.left.offset)
			qu.add(start.left);
		if (start.right != null && start.right.offset)
			qu.add(start.right);

		while (!qu.isEmpty()) {
			int size = qu.size();
			len++;
			while (size-- > 0) {
				Node cur = qu.poll();

				if (cur.auth >= len) {
					result++;
				}

				if (cur.left != null && cur.left.offset)
					qu.add(cur.left);
				if (cur.right != null && cur.right.offset)
					qu.add(cur.right);

			}
		}

		return result;

	}

	// 부모를 교환해야 한다.
	public static void swap(int a, int b) {
		// 먼저 a의 부모의 child에 b를 넣고 b의 부모의 child에 a를 넣기
		if (tree[a].parent.left != null && tree[a].parent.left.num == a) {
			tree[a].parent.left = tree[b];
		} else if (tree[a].parent.right != null) {
			tree[a].parent.right = tree[b];
		}

		if (tree[b].parent.left != null && tree[b].parent.left.num == b) {
			tree[b].parent.left = tree[a];
		} else if (tree[b].parent.right != null) {
			tree[b].parent.right = tree[a];
		}

	}
}