/**
 * Resisterクラスを通じて文字列を登録することの失敗時に発生する例外
 * ファイルが存在しない・ファイルが読み込めない・登録したい文字列に不正な文字列が含まれている等の時に発生する
 *
 * @author hebo-MAI
 * @since 2012/10/13
 * @version 1.0
 *
 */
public class ResisterException extends Exception {
	/**
	 *
	 */
	private static final long serialVersionUID = 4328107603627645429L;

	public ResisterException(String message) {
		super(message);
	}
}
