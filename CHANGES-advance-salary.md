# Advance Salary — পরিবর্তন (১৭ নভেম্বর ২০২৬)

## ১. History থেকে কলম (Edit) চাপলে আজকের তারিখ আসবে
- আগে: History-র কোনো আবেদনের কলম চাপলে ফর্মে ওই আবেদনের পুরনো (সেভ করা) তারিখ বসত।
- এখন: কলম চাপলে ফর্মে **যেদিন চাপবেন সেদিনের তারিখ** (আজকের তারিখ, dd/MM/yyyy) বসবে।
- তারপর Update চাপলে রেকর্ডটাও আজকের তারিখেই সেভ হবে।
- History থেকে **Print / Share** চাপলে আগের মতোই পুরনো তারিখ থাকবে — ওটা বদলানো হয়নি, কারণ ওটা সেভ করা রেকর্ডের প্রিন্ট।

## ২. Applicant's Signature বাদ
- ভাউচারের নিচে বামদিকের **Applicant's Signature** লাইনটা আর থাকবে না।
- ডানদিকের **Authorized Signature** আগের মতোই থাকবে।
- Print / PDF / Share Image / Preview — সব জায়গাতেই একইভাবে বাদ গেছে।
- ফর্মের "Include Signature Lines" সুইচটা এখন শুধু Authorized Signature-র জন্য; তার নিচের লেখাটাও বদলে "Authorized signature line" করা হয়েছে।

## বদলানো ফাইল (১০টি — দুটো কপিতেই একই বদল)
zip-এর ভেতরে একই অ্যাপের দুটো কপি ছিল (`tools-main/app/...` আর `tools-main/tools-main/app/...`) — দুটোতেই একই বদল করা হয়েছে, যেটা দিয়েই Build করেন না কেন কাজ করবে:
1. `ui/AdvanceSalaryViewModel.kt` — নতুন ফাংশন `refreshDateToToday()`
2. `ui/AdvanceSalaryScreen.kt` — কলম চাপলে `refreshDateToToday()` ডাকা হয়
3. `util/AdvanceSalaryPrintUtils.kt` — PDF/প্রিন্ট থেকে Applicant's Signature বাদ
4. `util/AdvanceSalaryImageExporter.kt` — ছবি/প্রিভিউ থেকে Applicant's Signature বাদ
5. `ui/components/AdvanceSalaryFormCard.kt` — সুইচের নিচের লেখা বদল

## লাগানোর নিয়ম (জরুরি)
- এই zip-এর ভেতরের ফাইলগুলো আপনার প্রজেক্টের `tools-main/` ফোল্ডারের ওপর extract করে বসান (একই পথে যাবে)।
- তারপর **Android Studio-তে নতুন করে APK Build করে ফোনে install করতে হবে** — শুধু ফাইল বসালেই ফোনের অ্যাপ বদলাবে না। ফোনে যেটা আছে সেটা আগের Build, তাই ওটায় পুরনো তারিখ আর Applicant's Signature-ই দেখাবে।
- নতুন APK install করার পর History থেকে Razon Miazi-র কলম চাপলে আজকের তারিখ আসবে, আর প্রিভিউতে শুধু Authorized Signature থাকবে।

## একটা কথা
- এই কম্পিউটারে Android SDK নেই, তাই এখানে সরাসরি Build করে পরীক্ষা করা যায়নি — কোডের ব্র্যাকেট/সিনট্যাক্স মিলিয়ে দেখা হয়েছে। Android Studio-তে Build করলে নিশ্চিত হবে।
- Applicant's Signature পুরো প্রজেক্টে আর কোথাও আঁকা হয় না — Print, PDF, Share Image, Preview সব জায়গাতেই বাদ গেছে (দুটো কপিতেই)।
