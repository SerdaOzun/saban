<script lang="ts">
    import '../app.css';
    import MainNavbar from "../components/MainNavbar.svelte";

    let {children} = $props();

</script>

<div class="h-screen flex flex-col gap-0">
    <MainNavbar/>
    <main class="flex-grow pt-16 overflow-auto">
        {@render children()}
    </main>
</div>

