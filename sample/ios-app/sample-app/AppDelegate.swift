// @copyright Agoda Services Co. Ltd.

import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
        return true
    }
}

class DismissSegue: UIStoryboardSegue {
    override func perform() {
        guard let presentingViewController = source.presentingViewController else {
            return
        }
        presentingViewController.dismiss(animated: true, completion: nil)
    }
}

class ViewController: UIViewController {
    @IBAction func crash() {
        fatalError()
    }
}
