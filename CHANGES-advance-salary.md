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

## বদলানো ফাইল (৫টি)
1. `app/src/main/java/com/example/ui/AdvanceSalaryViewModel.kt` — নতুন ফাংশন `refreshDateToToday()`
2. `app/src/main/java/com/example/ui/AdvanceSalaryScreen.kt` — কলম চাপলে `refreshDateToToday()` ডাকা হয়
3. `app/src/main/java/com/example/util/AdvanceSalaryPrintUtils.kt` — PDF/প্রিন্ট থেকে Applicant's Signature বাদ
4. `app/src/main/java/com/example/util/AdvanceSalaryImageExporter.kt` — ছবি/প্রিভিউ থেকে Applicant's Signature বাদ
5. `app/src/main/java/com/example/ui/components/AdvanceSalaryFormCard.kt` — সুইচের নিচের লেখা বদল

## লাগানোর নিয়ম
- এই zip-এর ভেতরের ফাইলগুলো আপনার প্রজেক্টের `tools-main/` ফোল্ডারের ওপর extract করে বসান (একই পথে যাবে)।
- তারপর Android Studio-তে Build করে নিন।

## একটা কথা
- এই কম্পিউটারে Android SDK নেই, তাই এখানে সরাসরি Build করে পরীক্ষা করা যায়নি — কোডের ব্র্যাকেট/সিনট্যাক্স মিলিয়ে দেখা হয়েছে। Android Studio-তে Build করলে নিশ্চিত হবে।
- zip-এর ভেতরে `tools-main/` নামে আরেকটা পুরনো কপি আছে — ওটা Build-এ ব্যবহার হয় না, তাই ওটায় কিছু বদলানো হয়নি।
