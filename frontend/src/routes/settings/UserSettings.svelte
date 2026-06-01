<script lang="ts">
  import Label from "@/components/ui/label/label.svelte";
  import { loggedInUser } from "../../data/stores/UserStore";
  import SearchableCombobox from "../../components/SearchableCombobox.svelte";
  import { countries } from "../../util/countries";
  import { onMount } from "svelte";
  import { toast } from "svelte-sonner";
  import { Toaster } from "@/components/ui/sonner";
  import { backendUrl } from "../../util/GuiConfig";
  import SpokenLanguages from "./SpokenLanguages.svelte";

  let oldCountry: string | null = $state(null);
  let selectedCountry: string | null = $state(null);
  let spokenLanguages: Array<string> = $state([]);

  interface SettingsResponse {
    country: string | null;
    spokenLanguages: Array<string>;
  }

  onMount(() => {
    fetchCountry();
  });

  $effect(() => {
    if (oldCountry !== selectedCountry) {
      setCountry();
    }
  });

  async function fetchCountry() {
    const resp = await fetch(`${backendUrl}/gui/settings/data`, {
      credentials: "include",
    });

    if (resp.ok) {
      let settings: SettingsResponse = await resp.json();
      selectedCountry = settings.country;
      oldCountry = settings.country;
      spokenLanguages = settings.spokenLanguages;
    }
  }

  async function setCountry() {
    if (!selectedCountry) return;

    const resp = await fetch(
      `${backendUrl}/gui/settings/country/${selectedCountry}`,
      {
        method: "POST",
        credentials: "include",
      },
    );

    if (resp.ok) {
      oldCountry = selectedCountry;
      toast.success("Country has been saved", {
        description: "",
      });
    }
  }
</script>

<div class="flex flex-col gap-2">
  {#if $loggedInUser}
    <Label class="text-2xl">Hello {$loggedInUser.username}</Label>
  {/if}
  <div class="flex w-fit flex-wrap place-items-center gap-2">
    <Label class="text-lg">Country:</Label>
    <SearchableCombobox options={countries} bind:selected={selectedCountry} />
  </div>
  <SpokenLanguages {spokenLanguages}/>
</div>

<Toaster />
